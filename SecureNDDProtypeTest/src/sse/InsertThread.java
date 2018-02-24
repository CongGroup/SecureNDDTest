package sse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import sse.CounterKeyTuple;
import base.RawRecord;
import secure.PRF;
import base.HammingLSH;
import sse.MathTool;
import base.MyCountDown;

public class InsertThread extends Thread {
	
	private static Integer lock = 1;

	private MyCountDown threadCounter;
	
	private String keyHat;
	
	private String key;
	
	private String keyU;
	
	private Map<String, String> mapIndex;
	
	private Map<String, CounterKeyTuple> mapH;
	
	private Map<String, Long> mapHcrtID;
	
	private List<RawRecord> rawDataset;
	
	private HammingLSH lsh;

	public InsertThread(String threadName, MyCountDown threadCounter, String keyHat, String key, String keyU, Map<String, String> mapIndex,
			Map<String, CounterKeyTuple> mapH, Map<String, Long> mapHcrtID, List<RawRecord> rawDataset, HammingLSH lsh) {
		super(threadName);
		this.threadCounter = threadCounter;
		this.keyHat = keyHat;
		this.key = key;
		this.keyU = keyU;
		this.mapIndex = mapIndex;
		this.mapH = mapH;
		this.mapHcrtID = mapHcrtID;
		this.rawDataset = rawDataset;
		this.lsh = lsh;
	}
	
	public void run() {

		System.out.println(getName() + " is running! data size = " + rawDataset.size());
		
		for (int i = 0; i < rawDataset.size(); ++i) {
			
			RawRecord record =  rawDataset.get(i);
			
			long[] u = lsh.computeLSH(record.getValue());
			
			int id = record.getId();
			
			String strKID = PRF.F(keyHat, String.valueOf(id));
			
			for (int l = 0; l < lsh.getL(); ++l) {
				
				String tHat  = String.valueOf(u[l]) + l;
				String strK1 = PRF.F1(key, tHat);
				String strK2 = PRF.F2(key, tHat);
				
//				Long crtID  = null;
//				CounterKeyTuple ctrKeyTuple = null;
				
				String strK3 = null;
				String strK4 = null;
				
				synchronized (lock) {
					// forward index counter updates
					//crtID = mapHcrtID.get(strKID);
					if (mapHcrtID.containsKey(strKID)) {
						//++crtID;
						mapHcrtID.put(strKID, mapHcrtID.get(strKID)+1);
					} else {
						//crtID = 1L;
						mapHcrtID.put(strKID, 1L);
					}
					
					// inverted index counter updates
					//ctrKeyTuple = mapH.get(tHat);
					if (mapH.containsKey(tHat)) {
						//ctrKeyTuple.setCrt(ctrKeyTuple.getCrt()+1);
						mapH.get(tHat).setCrt(mapH.get(tHat).getCrt()+1);
					} else {
						//ctrKeyTuple = new CounterKeyTuple(1L, 0L, key, keyU);
						mapH.put(tHat, new CounterKeyTuple(1L, 0L, key, keyU));
					}
					
//					// forward index counter updates
//					crtID = mapHcrtID.get(strKID);
//					if (crtID != null && crtID > 0) {
//						++crtID;
//						mapHcrtID.put(strKID, crtID);
//					} else {
//						crtID = 1L;
//						mapHcrtID.put(strKID, crtID);
//					}
//					
//					// inverted index counter updates
//					ctrKeyTuple = mapH.get(tHat);
//					if (ctrKeyTuple != null) {
//						ctrKeyTuple.setCrt(ctrKeyTuple.getCrt()+1);
//						mapH.put(tHat, ctrKeyTuple);
//					} else {
//						ctrKeyTuple = new CounterKeyTuple(1L, 0L, key, keyU);
//						mapH.put(tHat, ctrKeyTuple);
//					}
					
					strK3 = PRF.F3(strKID, String.valueOf(mapHcrtID.get(strKID)));
					strK4 = PRF.F4(strK1, String.valueOf(mapH.get(tHat).getCrt()));
				}
				

				// TODO: double check the K_2
				int cipher  = MathTool.encryptValue(strK2.getBytes(), id);
				
				mapIndex.put(strK3, strK4);
				mapIndex.put(strK4, strK3 + "::" + cipher);
				
			}
			
			if ((i+1)%(rawDataset.size()/10) == 0) {
				double  p  =  (i + 1.0)  /  rawDataset.size();
		        NumberFormat nf  =  NumberFormat.getPercentInstance();
		        nf.setMinimumFractionDigits( 0 );
				System.out.println("Insert ------ " + nf.format(p) + ", mapIndex.size() = " + mapIndex.size());
			}
		}
		
		System.out.println(getName() + " is finished!");

		threadCounter.countDown();
	}
}
