package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import base.ContentProvider;
import base.Distance;
import base.EncryptedFingerprint;
import base.HammingLSH;
import base.MyCountDown;
import base.Parameters;
import base.RawRecord;
import base.Repository;
import base.SingleRepoInsertThread;
import base.SysConstant;
import base.ThirdParty;
import base.User;
import it.unisa.dia.gas.jpbc.Element;
import secure.HashElGamal;
import secure.HashElGamalCiphertext;
import util.ConfigParser;
import util.FileTool;
import util.MyAnalysis;
import util.PrintTool;

/**
 * For performance evaluation, we just use one repository and involve the ranking mechanism.
 * 
 * Inside each repo, the searching operation is based on linear scanning, which is consistent with the original MKSE scheme.
 * 
 * The thread is in "L" level, i.e., each column is checking in a thread.
 * 
 * Note: here the ground truth is determined by a given plaintext's threshold.
 * 
 * @author Helei Cui
 * 
 * Modified by CHU Yilei on 2015 June 20
 * Update:  1570 images * 9 = 14,130 resized images
 * 			Using 157 original images to do 157 queries 
 * 			(query fingerprints in ahash_fp_jpeg_query.txt generated by BatchGenFingerprint.java)
 * 			For each query's result list (after voting), examine top 1, top 5, top 10, top 20, top 30... respectively
 * 			to get the average true positive rate.
 * 
 * 
 * Modified by Helei CUI on 2015 Nov. 28
 * Update: finalize the code for prototype demonstration.
 *
 */
public class TestPrototypeInCiphertext {

	public static void main(String[] args) {

		if (args.length < 1) {

			PrintTool.println(PrintTool.ERROR,
					"please check the argument list!");

			return;
		}

		ConfigParser config = new ConfigParser(args[0]);

		String inputPath = config.getString("inputPath").replace("\\", "/");
		String dbFileName = config.getString("dbFileName");
		String queryFileName = config.getString("queryFileName");

		int numOfLimit = config.getInt("numOfLimit");
		
		// In this test, we just use 1 repository.
		int numOfRepo = config.getInt("numOfRepo");
		String pairingSettingPath = config.getString("pairingSettingPath");
		
		int bitLength = config.getInt("bitLength");
		int certainty = config.getInt("certainty");
		
		int lshL = config.getInt("lshL");
		int lshDimension = config.getInt("lshDimension");
		int lshK = config.getInt("lshK");
		int threshold = config.getInt("threshold");
		
		boolean isCached = config.getBool("isCached");
		
		// Step 1: preprocess: setup keys and read file
		Parameters params = new Parameters(pairingSettingPath, lshL, lshDimension, lshK, bitLength, certainty);
		
		// initialize the lsh functions.
		HammingLSH lsh = new HammingLSH(lshDimension, lshL, lshK);
		
		System.out.println(">>> System parameters have been initialized.\n");
		System.out.println(">>> Now, reading the raw db data from " + inputPath + dbFileName);
		
		// read file to lines list
		List<RawRecord> rawRecords = FileTool.readFingerprintFromFile2ListV2(inputPath, dbFileName, numOfLimit, false);
		
		List<RawRecord> queryRecords = FileTool.readFingerprintFromFile2ListV2(inputPath, queryFileName, numOfLimit, false);

		if (numOfLimit > rawRecords.size()) {
			numOfLimit = rawRecords.size();
		}

		// Step 2: initialize the users, the repositories and secure insert records
		System.out.println(">>> There are " + numOfLimit + " records.");
		System.out.println(">>> Now, initializing " + numOfRepo + " repositories.");
		
		// initialize a third party to simulate GC operations in this prototype testing.
		ThirdParty thirdParty = new ThirdParty(params);
		
		// initialize a user with uid 1.
		User detector = new User(1, params, thirdParty.getKeyPublic());
		
		// initialize a content provider with cpid 1.
		ContentProvider contentProvider = new ContentProvider(1, params, thirdParty.getKeyPublic());
		
		// the repository id is 1.
		int rid = 1;
		
		Repository repo = new Repository(rid, params, contentProvider.getKeyV(), contentProvider.getKeyPublic());
		
		long stOfAuth = System.nanoTime();
		
		// authorize the detector
		Element delta = contentProvider.authorize(detector.getKeyVForAuthorization());
		
		long etOfAuth = System.nanoTime();
		
		System.out.println("Avg auth time is:" + (double)(etOfAuth - stOfAuth) / 1000000 + " ms.");
					
		// id = 1 is the detector
		repo.addDelta(detector.getUid(), delta);
		
		System.out.println(">>> Now, start adding data into repositories...");
		
		long startTimeOfInsert = System.currentTimeMillis();
		
		// Compute LSH
		List<Map<Integer, Long>> lshVectors = batchComputeLSH(rawRecords, params);
		System.out.println(">> LSH converted.");
		
		// Encrypt fingerprints
		encryptFP(rawRecords, params, repo);
		System.out.println(">> fingerprints encrypted.");
		
		//multiple threads
        MyCountDown threadCounter = new MyCountDown(lshL);

        for (int i = 0; i < lshL; i++) {

        	SingleRepoInsertThread t = new SingleRepoInsertThread("Thread " + i, threadCounter, repo.getKeyV(), lshVectors.get(i), repo.getSecureRecords().get(i), params);

            t.start();
        }

        // wait for all threads done
        while (true) {
            if (!threadCounter.hasNext())
                break;
        }
        
        long etOfInsert = System.currentTimeMillis();
        
        System.out.println("Insert time: " + (etOfInsert - startTimeOfInsert) + " ms.");

		// %%%%%%%%%%%%%%%%%% test %%%%%%%%%%%%%%%%%%%

		if (rawRecords == null || rawRecords.isEmpty()) {

			PrintTool.println(PrintTool.ERROR,
					"reading failed, please check the input file!");

			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean rootFlag = true;

		while (rootFlag) {
			System.out
					.print("\n\n----------------------- Root Menu -----------------------\n"
							+ "Please select an operation:\n"
							+ "[1]  query test;\n"
							//+ "[2]  analyze recall and precision;\n"
							+ "[2]  analyze top-k;\n"
							+ "[3]  analyze CDF;\n"
							+ "[4]  average located items;\n"
							//+ "[6]  throughput;\n"
							+ "[QUIT] quit system.\n\n"
							+ "--->");
			String inputStr;
			int operationType;
			try {
				inputStr = br.readLine();

				try {
					if (inputStr == null
							|| inputStr.toLowerCase().equals("quit")
							|| inputStr.toLowerCase().equals("q")) {

						System.out.println("Quit!");

						break;
					} else if (Integer.parseInt(inputStr) > 4
							|| Integer.parseInt(inputStr) < 1) {

						System.out.println("Warning: operation type should be limited in [1, 4], please try again!");

						continue;
					} else {
						operationType = Integer.parseInt(inputStr);
					}
				} catch (NumberFormatException e) {
					System.out.println("Warning: operation type should be limited in [1, 4], please try again!");

					continue;
				}

			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			switch (operationType) {
			case SysConstant.OPERATION_QUERY:
				queryInCiphertext(queryRecords, rawRecords, detector, repo, thirdParty, params, lsh, lshL, threshold, isCached);
				break;
			case SysConstant.OPERATION_ANALYZE_TOP_K:
				analyzeTopKInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			case SysConstant.OPERATION_ANALYZE_CDF:
				analyzeCDFInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			case SysConstant.OPERATION_COUNT_ITEMS:
				countItemsInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			default:
				break;
			}
		}
	}
	
	private static void queryInCiphertext(List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, User detector, Repository repo, ThirdParty thirdParty, Parameters params, HammingLSH lsh, int lshL, int threshold, boolean isCached) {

		System.out.println("\nModel: query.");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out
					.println("\n\nNow, you can search by input you query id range from [1, "
							+ queryRecords.size()
							+ "]: (-1 means return to root menu)");

			String queryStr = null;
			int queryIndex;
			RawRecord queryRecord;
			
			try {
				queryStr = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (queryStr == null || queryStr.equals("-1")) {

					System.out.println("Return to root menu!");

					break;
				} else if (Integer.parseInt(queryStr) > queryRecords
						.size() || Integer.parseInt(queryStr) <= 0) {

					System.out
							.println("Warning: query index should be limited in [1, limit]");

					continue;
				} else {
					queryIndex = Integer.parseInt(queryStr);
					
					queryRecord = queryRecords.get(queryIndex-1);

					System.out.println("For query item id : "
							+ queryRecord.getId() + ", name : " + queryRecord.getName() + ", fingerprint : " + queryRecord.getValue());
				}
			} catch (NumberFormatException e) {
				System.out
						.println("Warning: query index should be limited in [1, "
								+ rawDBRecords.size() + "]");
				continue;
			}

			long stOfGenQuery = System.currentTimeMillis();
			
			// prepare the query message
			List<Element> tArray = new ArrayList<Element>(lshL);
			

			long[] lshVector = lsh.computeLSH(queryRecord.getValue());
			
			for (int i = 0; i < lshL; i++) {
				
				Element t = params.h1Pre.pow(BigInteger.valueOf(lshVector[i])).powZn(detector.getKeyV());
				
				tArray.add(t);
			}
			
			long etOfGenQuery = System.currentTimeMillis();
			
			System.out.println("Time cost of generate query: " + (etOfGenQuery - stOfGenQuery) + " ms.");
			
			long time1 = System.currentTimeMillis();
			
			Map<Integer, Integer> searchResult = repo.secureSearch(detector.getUid(), tArray, isCached);
			
			long time2 = System.currentTimeMillis();

			System.out.println("Cost " + (time2 - time1) + " ms.");
			
			long avgDecTime = 0;
			
			if (searchResult != null && searchResult.size() > 0) {
				
				for (Map.Entry<Integer, Integer> entry : searchResult.entrySet()) {

					int id = entry.getKey();
					int counter = entry.getValue();
					
					EncryptedFingerprint item = repo.getEncryptedFingerprints().get(id);
					
					BigInteger plainFP;
					try {
						
						long stOfDec = System.nanoTime();
						
						//plainFP = Paillier.Dec(item.getCipherFP(), repo.getKeyF(), thirdParty.getKeyPrivate());
						plainFP = HashElGamal.decrypt(params.hashElGamalPara, thirdParty.getKeyPrivate(), item.getCipherFP());
						
						long etOfDec = System.nanoTime();
						
						avgDecTime += etOfDec - stOfDec;
						
						int dist = Distance.getHammingDistanceV2(queryRecord.getValue(), plainFP);
						
						if (dist > threshold) {
							
							continue;
						}
						
						System.out.println(id + " :: " + item.getName() + " :: " + plainFP + " >>> dist: " + dist + "  Counter::" + counter);	
						//System.out.println("Counter::" + counter);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
				
				System.out.println("Avg dec time is:" + (double)avgDecTime/searchResult.size() / 1000000 + " ms.");
				
			} else {
				System.out.println("No similar item!!!");
			}
			
			// print the statistics
			//System.out.println("The recall is : " + MyAnalysis.computeRecall(queryRecord.getName(), searchResult, rawRecords, numOfPositive));
			
			//System.out.println("The precision is : " + MyAnalysis.computePrecision(queryRecord.getName(), searchResult, rawRecords));
		}
	}

	
	private static void analyzeTopKInCiphertext(List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, User detector, Repository repo, Parameters params, HammingLSH lsh, int lshL, int threshold, boolean isCached) {

		RawRecord queryRecord;
		
		float avgGenTokenTime = 0;
		
		long avgSearchTime = 0;
						
		int avgNumOfCandidate = 0;
		
		int queryTimes = 0;
		
		int[] topK_list = {1,3,5,8,10,13,15,18,20,30,40,50}; // the choice of K for "top K"
		
		float[] avgAccuracy = new float[topK_list.length];
		
		for(int j=0; j<avgAccuracy.length;++j)
			avgAccuracy[j] = 0.0F;
		
		for (int i = 0; i < queryRecords.size(); i++) {
			
			queryRecord = queryRecords.get(i);
			
			++queryTimes;
			System.out.println(queryTimes);
			
			long stOfGenToken = System.currentTimeMillis();
			
			// prepare the query message
			List<Element> Q = new ArrayList<Element>(lshL);

			long[] lshVector = lsh.computeLSH(queryRecord.getValue());
			
			for (int j = 0; j < lshL; j++) {
				
				Element t = params.h1Pre.pow(BigInteger.valueOf(lshVector[j])).powZn(detector.getKeyV());
				
				Q.add(t);
			}
			
			long etOfGenToken = System.currentTimeMillis();
			
			long time1 = System.currentTimeMillis();
			
			Map<Integer, Integer> searchResult = repo.secureSearch(detector.getUid(), Q, isCached);
			
			// rank the result
			List<Entry<Integer, Integer>> rankedList = new ArrayList<Entry<Integer, Integer>>(searchResult.entrySet());   
			  
			Collections.sort(rankedList, new Comparator<Object>(){   
			          @SuppressWarnings("unchecked")
					public int compare(Object e1, Object e2){   
			        int v1 = ((Entry<Integer, Integer>)e1).getValue();   
			        int v2 = ((Entry<Integer, Integer>)e2).getValue();   
			        return v2-v1;   
			           
			    }   
			}); 
			
			long time2 = System.currentTimeMillis();

			avgGenTokenTime += etOfGenToken - stOfGenToken;
			
			avgSearchTime += time2 - time1;
			
			avgNumOfCandidate += searchResult.size();
			
			// 2015 06 20 Update: for different topK, calculate a precision & recall 
			
			for (int j = 0; j < topK_list.length; j++) {
				
				int topK = topK_list[j];
				
				if (rankedList.size() < topK) {
					topK = rankedList.size();
				}
				
				avgAccuracy[j] += MyAnalysis.computeTopK(queryRecord, rankedList.subList(0, topK), rawDBRecords, threshold);
			}
		
		}
		
		// print the statistics
		//System.out.println("Average recall is        : " + avgRecall/queryTimes*100 + " %");
		for (int j = 0; j < topK_list.length; j++) {
			
			System.out.println("Average accuracy of top-" + topK_list[j] + " is     : " + avgAccuracy[j]/queryTimes*100 + " %");
		}
		
		
		System.out.println("\nAverage genToken time is : " + avgGenTokenTime/(float)queryTimes + " ms");
		System.out.println("Average search time is   : " + avgSearchTime/(float)queryTimes + " ms");
		System.out.println("Average candidate size   : " + avgNumOfCandidate/queryTimes);
	}
	
	private static void analyzeCDFInCiphertext(List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, User detector, Repository repo, Parameters params, HammingLSH lsh, int lshL, int threshold, boolean isCached) {

		RawRecord queryRecord;
		
		int[] numOfRetrieval_list = {1,5,10,15,20,30,40,50};
		
		int[][] cntRecord = new int[numOfRetrieval_list.length][queryRecords.size()];
		
		//initialize result 2d array to 0
		for (int i = 0; i < numOfRetrieval_list.length; i++) 
			for (int j = 0; j < queryRecords.size(); j++) 
				cntRecord[i][j] = 0;
						
		for (int i = 0; i < queryRecords.size(); i++) {
			
			queryRecord = queryRecords.get(i);
			
			// prepare the query message
			List<Element> Q = new ArrayList<Element>(lshL);
			
			long[] lshVector = lsh.computeLSH(queryRecord.getValue());
			
			for (int j = 0; j < lshL; j++) {
				
				Element t = params.h1Pre.pow(BigInteger.valueOf(lshVector[j])).powZn(detector.getKeyV());
				
				Q.add(t);
			}
			
			Map<Integer, Integer> searchResult = repo.secureSearch(detector.getUid(), Q, isCached);
			
			// rank the result
			List<Entry<Integer, Integer>> rankedList = new ArrayList<Entry<Integer, Integer>>(searchResult.entrySet());   
			  
			Collections.sort(rankedList, new Comparator<Object>(){   
			          @SuppressWarnings("unchecked")
					public int compare(Object e1, Object e2){   
			        int v1 = ((Entry<Integer, Integer>)e1).getValue();   
			        int v2 = ((Entry<Integer, Integer>)e2).getValue();   
			        return v2-v1;   
			           
			    }   
			}); 
							
			
			// 2015 06 20 Update: for different numOfRetrieval, print cntCompare per query 					
			for (int j = 0; j < numOfRetrieval_list.length; j++) {						
				int numOfRetrieval = numOfRetrieval_list[j];
				
				if (rankedList.size() <= numOfRetrieval) {
					cntRecord[j][i] = rankedList.size();
				}else {
					cntRecord[j][i] = MyAnalysis.computeCDF(queryRecord, rankedList, rawDBRecords, threshold,numOfRetrieval);
				}						
			}									
		} // end of query
		
        //print result to file
		BufferedWriter writer = null;
        
        try {
			writer = new BufferedWriter(new FileWriter("./cdfResult.txt", false));
        
	        for (int i = 0; i < numOfRetrieval_list.length; i++)
	        {
				for (int j = 0; j < queryRecords.size(); j++) 
					writer.write(cntRecord[i][j]+";");
				writer.write("\n\n\n");
	        }
	        
	        writer.close();
        	
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	private static void countItemsInCiphertext(List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, User detector, Repository repo, Parameters params, HammingLSH lsh, int lshL, int threshold, boolean isCached) {

		RawRecord queryRecord;
		
		int[] numOfItemsInThreshold = new int [threshold+1];

		int queryTimes = 0;
		
		long avgGenQueryTime = 0;
		
		long avgSearchTime = 0;

		for (int i = 0; i < queryRecords.size(); i++) {

			queryRecord = queryRecords.get(i);
			
			System.out.println(++queryTimes);
			
			long stOfGenQuery = System.nanoTime();
			
			// prepare the query message
			List<Element> Q = new ArrayList<Element>(lshL);

			long[] lshVector = lsh.computeLSH(queryRecord.getValue());
			
			for (int j = 0; j < lshL; j++) {
				
				Element t = params.h1Pre.pow(BigInteger.valueOf(lshVector[j])).powZn(detector.getKeyV());
				
				Q.add(t);
			}
			
			long etOfGenQuery = System.nanoTime();
			
			avgGenQueryTime += etOfGenQuery - stOfGenQuery;
			
			long stSearchTime = System.currentTimeMillis();
			
			Map<Integer, Integer> searchResult = repo.secureSearch(detector.getUid(), Q, isCached);
			
			long etSearchTime = System.currentTimeMillis();
			
			avgSearchTime += etSearchTime - stSearchTime;
			
			int[] tmpResult = new int[threshold+1];
			
			for (Map.Entry<Integer, Integer> entry : searchResult.entrySet()) {

				int id = entry.getKey();
				
				int dist = Distance.getHammingDistanceV2(queryRecord.getValue(), rawDBRecords.get(id-1).getValue());
				
				if (dist <= threshold) {
					
					for (int j = dist; j <= threshold; j++) {
						tmpResult[j]++;
					}
				}
			}
			
			for (int j = 0; j <= threshold; j++) {
				
				numOfItemsInThreshold[j] += tmpResult[j];
			}
			
		}
		
		System.out.println("Avg gen query time is:" + (double)avgGenQueryTime/queryRecords.size() / 1000000 + " ms.");
		System.out.println("Avg search time is:" + (double)avgSearchTime/queryRecords.size() + " ms.");
		// print the statistics
		System.out.println("Average located items' number are:\n");
		
		for (int i = 0; i <= threshold; i++) {
			
			System.out.println("threshold <= " + i + ": " + numOfItemsInThreshold[i]/queryRecords.size());
		}
	}
	
	private static List<Map<Integer, Long>> batchComputeLSH(List<RawRecord> rawRecords,
			Parameters params) {
		
		List<Map<Integer, Long>> lshVectorsInL = new ArrayList<Map<Integer, Long>>(params.lshL);

		for (int i = 0; i < params.lshL; i++) {
			lshVectorsInL.add(new HashMap<Integer, Long>());
		}

		for (int i = 0; i < rawRecords.size(); i++) {
			
			RawRecord rd = rawRecords.get(i);

			// compute LSH vector
			long[] lshVector = params.lsh.computeLSH(rd.getValue());
			
			for (int j = 0; j < lshVector.length; j++) {
				lshVectorsInL.get(j).put(rd.getId(), lshVector[j]);
			}
		}
		return lshVectorsInL;
	}
	
	private static void encryptFP(List<RawRecord> rawRecords, Parameters params, Repository repo) {

		//Map<Integer, EncryptedFingerprint> encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>();
		
		for (int i = 0; i < rawRecords.size(); i++) {
			
			// encrypt fingerprint
			//BigInteger cipherFP = Paillier.Enc(rawRecords.get(i).getValue(), repo.getKeyF());
			HashElGamalCiphertext cipherFP = HashElGamal.encrypt(params.hashElGamalPara, repo.getKeyF(), rawRecords.get(i).getValue());
			
			repo.getEncryptedFingerprints().put(rawRecords.get(i).getId(),
					new EncryptedFingerprint(rawRecords.get(i).getName(), cipherFP));
		}
	}
}
