package sse;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sse.CounterKeyTuple;
import sse.GenTokenResult;
import sse.K1K2Pair;
import base.RawRecord;
import sse.TokenTuple;
import secure.PRF;
//import secure.PaillierPublicKey;
import base.HammingLSH;
import sse.FileTool;
import sse.MathTool;
import base.MyCountDown;

public class IndexBuilder {

	private String                       keyHat;
	private String                       key;
	private String                       keyU;
	
	private Map<String, String>          mapIndex;// = new ConcurrentHashMap<>();
	
	private Map<String, CounterKeyTuple> mapH;// = new ConcurrentHashMap<>();
	
	private Map<String, Long>            mapHcrtID;// = new ConcurrentHashMap<>();
	
	private BigInteger keyPublic;
	
	public IndexBuilder(String keyHat, String key, String keyU, BigInteger keyPublic) {

		this.keyHat = keyHat;
		this.key    = key;
		this.keyU   = keyU;
		this.keyPublic = keyPublic;
	}
	
	public void buildEncryptedIndex(List<RawRecord> rawDataset, HammingLSH lsh, int threadNum) {
		
		this.mapIndex  = new ConcurrentHashMap<>();
		this.mapH      = new ConcurrentHashMap<>();
		this.mapHcrtID = new ConcurrentHashMap<>();
		
		MyCountDown threadCounter = new MyCountDown(threadNum);
		
		int partialNum = rawDataset.size() / threadNum;

        for (int i = 0; i < threadNum; i++) {

        	InsertThread t = null;
        	if (i != threadNum - 1) {
        		//System.out.println(i*partialNum + " --- " + (i+1)*partialNum);
            	t = new InsertThread("Thread " + i, threadCounter, keyHat, key, keyU, mapIndex, mapH, mapHcrtID, rawDataset.subList(i*partialNum, (i+1)*partialNum), lsh);

			} else {
				//System.out.println(i*partialNum + " --- " + rawDataset.size());
	        	t = new InsertThread("Thread " + i, threadCounter, keyHat, key, keyU, mapIndex, mapH, mapHcrtID, rawDataset.subList(i*partialNum, rawDataset.size()), lsh);

			}
        	
            t.start();
        }
        
        // wait for all threads done
        while (true) {
            if (!threadCounter.hasNext())
                break;
        }
	}
	
	public GenTokenResult genSearchToken(RawRecord record, HammingLSH lsh, int searchNum) {
		
		// TODO: change this to random keys
		String nKeyU = "keyU" + searchNum;
		String nKey  = "key"  + searchNum;
		
		GenTokenResult gtr = new GenTokenResult();
		
		int lshL = lsh.getL();
		
		long[] u = lsh.computeLSH(record.getValue());
		
		int counterI = 0;
		for (int l = 0; l < lshL; ++l) {
			
			String tHat = String.valueOf(u[l]) + l;

			if (mapH.containsKey(tHat)) {
				
				CounterKeyTuple ctrKeyTuple = mapH.get(tHat);
				
				key       = ctrKeyTuple.getK();
				keyU      = ctrKeyTuple.getKu();
				
				//System.out.println("keyU = " + keyU);
				
				long crt  = ctrKeyTuple.getCrt();
				long ucrt = ctrKeyTuple.getUcrt();
				
				ctrKeyTuple.setK(nKey);
				ctrKeyTuple.setKu(nKeyU);
				mapH.put(tHat, ctrKeyTuple);
				
				K1K2Pair t  = new K1K2Pair(PRF.F1(key,  tHat), PRF.F2(key,  tHat));
				K1K2Pair tu = new K1K2Pair(PRF.F1(keyU, tHat), PRF.F2(keyU, tHat));
				K1K2Pair nt = new K1K2Pair(PRF.F1(nKey, tHat), PRF.F2(nKey, tHat));
				
				TokenTuple tt = new TokenTuple(t, crt, tu, ucrt, nt);
				
				gtr.tokens.add(tt);
				gtr.tHats.add(tHat);
//				gtr.lList.add(l);
//				gtr.tHatMap.put(l, tHat);
				
				++counterI;
			}
		}
		
		if (counterI > lshL) {
			System.err.println("Error in token generation!");
		}
		
		return gtr;
	}
	
	public void updateCRTAfterSearch(List<String> tHatList, List<Long> ncrtList) {
		
		for (int i = 0; i < tHatList.size(); i++) {
			
			long ncrt = ncrtList.get(i);
			
			if (ncrt > 0) {
				
				String tHat = tHatList.get(i);
				
				CounterKeyTuple ctrKeyTuple = mapH.get(tHat);
				
				ctrKeyTuple.setCrt(ncrt);
				ctrKeyTuple.setUcrt(0L);
				mapH.put(tHat, ctrKeyTuple);
			}
		}
	}
	
	public Map<String, String> genDynamicAddIndex(List<RawRecord> rawDataset, HammingLSH lsh, int baseID) {
		
		Map<String, String> mapLocalIndex  = new ConcurrentHashMap<>();
		
		for (int i = 0; i < rawDataset.size(); ++i) {
			
			RawRecord record =  rawDataset.get(i);
			
			long[] u = lsh.computeLSH(record.getValue());
			
			int id = record.getId() + baseID;
			
			String strKID = PRF.F(keyHat, String.valueOf(id));
			
			for (int l = 0; l < lsh.getL(); ++l) {
				
				String tHat  = String.valueOf(u[l]) + l;

				
				// Long crtID = null;
				// CounterKeyTuple ctrKeyTuple = null;

				String strK3 = null;
				String strK4 = null;

				// forward index counter updates
				if (mapHcrtID.containsKey(strKID)) {
					// ++crtID;
					mapHcrtID.put(strKID, mapHcrtID.get(strKID) + 1);
				} else {
					// crtID = 1L;
					mapHcrtID.put(strKID, 1L);
				}

				// inverted index counter updates
				if (mapH.containsKey(tHat)) {
					
					mapH.get(tHat).setUcrt(mapH.get(tHat).getUcrt() + 1);
				} else {
					// ctrKeyTuple = new CounterKeyTuple(1L, 0L, key, keyU);
					mapH.put(tHat, new CounterKeyTuple(0L, 1L, key, keyU));
				}
				
				CounterKeyTuple ckt = mapH.get(tHat);
				
				String strK1 = PRF.F1(ckt.getKu(), tHat);
				String strK2 = PRF.F2(ckt.getKu(), tHat);
				
				//System.out.println("KeyU = " + ckt.getKu());

				strK3 = PRF.F3(strKID, String.valueOf(mapHcrtID.get(strKID)));
				strK4 = PRF.F4(strK1, String.valueOf(mapH.get(tHat).getUcrt()));

				// TODO: double check the K_2
				int cipher  = MathTool.encryptValue(strK2.getBytes(), id);
				
				mapLocalIndex.put(strK3, strK4);
				mapLocalIndex.put(strK4, strK3 + "::" + cipher);
				
			}
			
			if ((i+1)%(rawDataset.size()/10) == 0) {
				double  p  =  (i + 1.0)  /  rawDataset.size();
		        NumberFormat nf  =  NumberFormat.getPercentInstance();
		        nf.setMinimumFractionDigits( 0 );
				System.out.println("Add ------ " + nf.format(p));
			}
		}
		
		return mapLocalIndex;
	}

	public List<String> genDynamicDelToken(List<RawRecord> rawDataset, int baseID) {

		List<String> delTokenList = new ArrayList<>();
		
		for (int i = 0; i < rawDataset.size(); ++i) {
			
			RawRecord record =  rawDataset.get(i);
			
			int id = record.getId() + baseID;
			
			String strKID = PRF.F(keyHat, String.valueOf(id));
			
			if ((i+1)%(rawDataset.size()/10) == 0) {
				double  p  =  (i + 1.0)  /  rawDataset.size();
		        NumberFormat nf  =  NumberFormat.getPercentInstance();
		        nf.setMinimumFractionDigits( 0 );
				System.out.println("Add ------ " + nf.format(p));
			}
			
			delTokenList.add(strKID);
		}
		
		return delTokenList;
	}
	
	public void storeIndex(String indexFilePath) {
		
		System.out.println("Size of index = " + mapIndex.size());
		
		FileTool.writeToFile(indexFilePath, mapIndex);
	}
	
//	public void loadIndex(String indexFilePath) {
//		
//		mapIndex = FileTool.readFromFile(indexFilePath);
//	}
	
	public void storeCounterMaps(String hcrtFilePath, String hcrtidFilePath) {
		
		FileTool.writeToFile(hcrtFilePath, mapH);
		FileTool.writeToFile(hcrtidFilePath, mapHcrtID);
	}
	
	public void loadCounterMaps(String hcrtFilePath, String hcrtidFilePath) {
		
		mapH = FileTool.readFromFile(hcrtFilePath);
		mapHcrtID = FileTool.readFromFile(hcrtidFilePath);
	}

	public String getKeyHat() {
		return keyHat;
	}

	public void setKeyHat(String keyHat) {
		this.keyHat = keyHat;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKeyU() {
		return keyU;
	}

	public void setKeyU(String keyU) {
		this.keyU = keyU;
	}

	public BigInteger getKeyPublic() {
		return keyPublic;
	}

	public void setKeyPublic(BigInteger keyPublic) {
		this.keyPublic = keyPublic;
	}

	public Map<String, String> getMapIndex() {
		return mapIndex;
	}

}
