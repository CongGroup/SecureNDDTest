package throughput;

import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.EncryptedFingerprint;
import base.Parameters;
import base.SecureRecord;
import base.SecureToken;
import secure.PRF;
import secure.PaillierPublicKey;
import util.MyCounter;

public class RepositoryForThroughputTest {

	private int id;

	private Parameters params;
	
	// key to encrypt each LSH keyword (a.k.a. token)
	private Element keyV;
	
	// key to encrypt each fingerprint
	private PaillierPublicKey keyF;
	
	
	private List<SecureRecord> secureRecords;
	
	private Map<Integer, Element> deltas;
	
	//private Map<Integer, NameFingerprintPair> rawRecord;
	
	private Map<Integer, EncryptedFingerprint> encryptedFingerprints;
	
	public RepositoryForThroughputTest(RepositoryForThroughputTest repo) {
		
		this.id = repo.getId();
		this.params = new Parameters(repo.getParams());
		this.keyV = repo.getKeyV().duplicate();
		this.keyF = repo.keyF;
		this.secureRecords = new ArrayList<SecureRecord>(repo.getSecureRecords());
		this.deltas = new HashMap<Integer, Element>(repo.getDeltas());
		this.encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>(repo.getEncryptedFingerprints());
	}
	
	public RepositoryForThroughputTest(int id, Parameters params, Element keyV, PaillierPublicKey keyF) {
		
		this.id = id;
		this.params = new Parameters(params);
		//this.params = params;
		
		this.keyV = keyV;
		
		this.keyF = keyF;
		
		this.secureRecords = new ArrayList<SecureRecord>();
		
		this.deltas = new HashMap<Integer, Element>();
		this.encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>();
	}
	
	/**
	 * Authorize a legal user
	 * 
	 * @param id
	 */
	public void addDelta(int id, Element delta) {
		
		this.deltas.put(id, delta);
	}
	
	/**
	 * insert secure record
	 * 
	 * @param secureRecord
	 */
	public void insert(SecureRecord secureRecord) {
		
		this.secureRecords.add(secureRecord);
	}
	
	
	public void secureSearch(int uid, List<Element> tArray, int repoNum, MyCounter numOfTest, long stTime) {
		
		Map<Integer, Integer> searchResult = new HashMap<Integer, Integer>();
		
		long innerStTime;
		
		if (!this.deltas.containsKey(uid)) {
			
			System.out.println("This user has not been authorized in repository (id = " + this.id + ")!");
			
		} else {
			
			while (true) {
				
				innerStTime = System.currentTimeMillis();
				
				if (innerStTime >= stTime) {
					
					System.out.println(innerStTime);
					break;
				}
			}
			
			for (int i = 0; i < repoNum-1; i++) {
				
				Element delta = this.deltas.get(uid);
				
				String[] adjustedQuery = new String[tArray.size()];
				
				// adjust the query tokens
				for (int j = 0; j < tArray.size(); j++) {
					
					adjustedQuery[j] = params.pairing.pairing(tArray.get(j), delta)
					.toString();
					
				}
			}
			
			Element delta = this.deltas.get(uid);
			
			String[] adjustedQuery = new String[tArray.size()];
			
			// adjust the query tokens
			for (int i = 0; i < tArray.size(); i++) {
				
				adjustedQuery[i] = params.pairing.pairing(tArray.get(i), delta)
				.toString();
				
			}
			
			
			// linear scan the secure tokens in repo
	        List<List<Integer>> tempResults = new ArrayList<>(this.params.lshL);
	        
	        for (int i = 0; i < this.params.lshL; i++) {
	        	
	        	tempResults.add(new ArrayList<>());
			}
	        
	        long ii = 0;
	        while (true){
	        //for (int i = 0; i < secureRecords.size(); i++) {
	        	
	        	int idx = (int)(ii%secureRecords.size());
	        	
	        	List<SecureToken> sts = secureRecords.get(idx).getSecureTokens();
				
	        	for (int j = 0; j < sts.size(); j++) {
	        		
	        		SecureToken secureToken = sts.get(j);
	    			
	    			//long stOfMatchingTime = System.nanoTime();
	    			
	    			long c = PRF.HMACSHA1ToUnsignedInt(adjustedQuery[j], secureToken.getR());
	    			
	    			if (secureToken.getH() == c) {
	    				
	    				tempResults.get(j).add(idx);
	    			}
	    			//System.out.println(sts.size());
				}
	        	
	        	numOfTest.addOne();
	        	ii++;
	        	
	        	if (System.currentTimeMillis() - innerStTime > 60000) {
					break;
				}
			}
	        
	        
			
			for (int i = 0; i < this.params.lshL; i++) {
				
				for (int j = 0; j < tempResults.get(i).size(); j++) {
					
					int id = tempResults.get(i).get(j);
					
					if (searchResult.containsKey(id)) {
						
						searchResult.put(id, searchResult.get(id) + 1);
					} else {
						searchResult.put(id, 1);
					}
				}
			}
		}
	}
	
	public boolean checkMatch(List<Element> query, SecureRecord secureRecord, Element delta) {
		
		boolean result = false;
		
		List<SecureToken> tokens = secureRecord.getSecureTokens();
		
		assert(query.size() == tokens.size());
		
		for (int i = 0; i < query.size(); i++) {
			
			String at = params.pairing.pairing(query.get(i), delta).toString();
			
			long c = PRF.HMACSHA1ToUnsignedInt(at, tokens.get(i).getR());
			
			if (tokens.get(i).getH() == c) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Element getKeyV() {
		return keyV;
	}

	public void setKeyV(Element keyV) {
		this.keyV = keyV;
	}

	public Map<Integer, Element> getDeltas() {
		return deltas;
	}

	public void setDeltas(Map<Integer, Element> deltas) {
		this.deltas = deltas;
	}

	public Map<Integer, EncryptedFingerprint> getEncryptedFingerprints() {
		return encryptedFingerprints;
	}

	public void setEncryptedFingerprints(
			Map<Integer, EncryptedFingerprint> encryptedFingerprints) {
		this.encryptedFingerprints = encryptedFingerprints;
	}

	public Parameters getParams() {
		return params;
	}

	public void setParams(Parameters params) {
		this.params = params;
	}

	public PaillierPublicKey getKeyF() {
		return keyF;
	}

	public void setKeyF(PaillierPublicKey keyF) {
		this.keyF = keyF;
	}

	public List<SecureRecord> getSecureRecords() {
		return secureRecords;
	}

	public void setSecureRecords(List<SecureRecord> secureRecords) {
		this.secureRecords = secureRecords;
	}
}
