package test;

import java.io.BufferedReader;
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

import base.RawRecord;
import base.Repository;
import base.SecureToken;
import sse.SimSearchResult;
import base.ThirdParty;
import it.unisa.dia.gas.jpbc.Element;
import secure.HashElGamal;
import secure.HashElGamalCiphertext;
import secure.HashElGamalParameters;
import base.Distance;
import base.EncryptedFingerprint;
import sse.GenTokenResult;
import base.HammingLSH;
import base.Parameters;
import sse.IndexBuilder;
import sse.IndexSearcher;
import util.ConfigParser;
import util.MyAnalysis;
import sse.FileTool;
import util.PrintTool;

public class TestSSE {
	
	private static int numOfSearch = 0;
	
	public static void main(String[] args) {

		if (args.length < 1) {

			PrintTool.println(PrintTool.ERROR,
					"please check the argument list!");

			return;
		}

		ConfigParser config   = new ConfigParser(args[0]);

		String inputPath      = config.getString("inputPath").replace("\\", "/");

		int numOfThread       = config.getInt("numOfThread");
		int numOfLimit        = config.getInt("numOfLimit");

		int lshD              = config.getInt("lshDimension");
		int lshL              = config.getInt("lshL");
		int lshK              = config.getInt("lshK");

		String dbFilePath     = inputPath + config.getString("dbFileName");
		String queryFilePath  = inputPath + config.getString("queryFileName");

		String keyHat         = config.getString("keyHat");
		
		int bitLength         = config.getInt("bitLength");
		int certainty         = config.getInt("certainty");
		

		int threshold         = config.getInt("threshold");

		
		// Step 1: initialize LSH and keys
		HammingLSH lsh = new HammingLSH(lshD, lshL, lshK);
		
		System.out.println(">>> LSH has been initialized.\n");
		
		System.out.println(">>> Now, reading the raw db data from " + dbFilePath);
		
		// Step 2: read file to raw data <id, name, fingerprint> list
		List<RawRecord> rawRecords = FileTool.readFingerprintFromFile2ListV2(dbFilePath, numOfLimit, false);

		List<RawRecord> queryRecords = FileTool.readFingerprintFromFile2ListV2(queryFilePath, numOfLimit, false);

		if (numOfLimit > rawRecords.size()) {
			numOfLimit = rawRecords.size();
		}
		
		
		HashElGamalParameters params = new HashElGamalParameters(bitLength, certainty);
		// initialize a third party to simulate GC operations in this prototype testing.
		ThirdParty thirdParty = new ThirdParty(params);
		
		
		
		System.out.println(">>> There are " + numOfLimit + " records.");
		System.out.println(">>> Now, start building the index...");

		// Step 3: build SSE index
		
		IndexBuilder indexBuilder = new IndexBuilder(keyHat, "key0", "keyU0", thirdParty.getKeyPublic());
		
		Map<Integer, EncryptedFingerprint> encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>();
		
		long stBuild = System.currentTimeMillis();
		
		// Encrypt fingerprints
		encryptFP(rawRecords, encryptedFingerprints, params, indexBuilder.getKeyPublic());
		System.out.println(">>> fingerprints encrypted.");
		
		
		indexBuilder.buildEncryptedIndex(rawRecords, lsh, numOfThread);
		long etBuild = System.currentTimeMillis();
		
		System.out.println(">>> Done. Total build time (ms): " + (etBuild - stBuild) + "\n");
		
		
		//Step 4: search index
		
		IndexSearcher cNode = new IndexSearcher(1);
		cNode.loadIndex(indexBuilder);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean rootFlag = true;

		while (rootFlag) {
			System.out
					.print("\n\n----------------------- Root Menu -----------------------\n"
							+ "Please select an operation:\n"
							+ "[1]  query test;\n"
							+ "[2]  analyze top-k;\n"
							+ "[3]  search time;\n"
//							+ "[3]  analyze CDF;\n"
//							+ "[4]  average located items;\n"
							+ "[Q]  quit system.\n\n"
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
			case 1:
				search(indexBuilder, cNode, queryRecords, rawRecords, lsh, threshold);
				break;
			case 2:
				
				analyzeTopK(indexBuilder, cNode, queryRecords, rawRecords, lsh, threshold);
				//analyzeTopKInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			case 3:
				evaluateSearchTime(indexBuilder, cNode, queryRecords, rawRecords, lsh, threshold);
				//analyzeCDFInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			case 4:
				//countItemsInCiphertext(queryRecords, rawRecords, detector, repo, params, lsh, lshL, threshold, isCached);
				break;
			default:
				break;
			}
		}
		
//		System.out.println(">>> Now, storing index and other counter maps into files...");
//
//		// Step 4: store data in files
//		
//		indexBuilder.storeIndex(indexFilePath);
//		indexBuilder.storeCounterMaps(hcrtFilePath, hcrtidFilePath);
		
//		// ****** test serialization ******
//		ConcurrentHashMap<String, String> index_j = new ConcurrentHashMap<>();
//		for (int i = 0; i < rawRecords.size(); i++) {
//			String k = String.valueOf(rawRecords.get(i).getId());
//			String v = rawRecords.get(i).getName();
//			System.out.println(v);
//			index_j.put(k, v);
//		}
//		
//		FileTool.writeToFile(indexFilePath, index_j);
//		
//		ConcurrentHashMap<String, String> indexRead = FileTool.readFromFile(indexFilePath);
//		for (int i = 0; i < rawRecords.size(); i++) {
//			String k = String.valueOf(rawRecords.get(i).getId());
//			String v = rawRecords.get(i).getName();
//			if (!indexRead.get(k).equals(v)) {
//				System.out.println("Error!");
//			}
//		}
//		// ****** end of test serialization ******
	}
	
	private static void search(IndexBuilder CP, IndexSearcher IS, List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, HammingLSH lsh, int threshold) {

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
			sse.GenTokenResult gtr = CP.genSearchToken(queryRecord, lsh, ++numOfSearch);
			
			long etOfGenQuery = System.currentTimeMillis();
			
			System.out.println("Time cost of generate query: " + (etOfGenQuery - stOfGenQuery) + " ms.");
			
			long time1 = System.currentTimeMillis();
			
			SimSearchResult ssr = IS.simSearch(gtr.tokens);
			
			Map<Integer, Integer> searchResult = ssr.idMap;
			
			long time2 = System.currentTimeMillis();

			System.out.println("Cost " + (time2 - time1) + " ms.");
			
			CP.updateCRTAfterSearch(gtr.tHats, ssr.ncrtList);
			
			System.out.println("key = " + CP.getKey() + ", keyU = " + CP.getKeyU());
			
			//long avgDecTime = 0;
			
			if (searchResult != null && searchResult.size() > 0) {
				
				for (Map.Entry<Integer, Integer> entry : searchResult.entrySet()) {

					int id = entry.getKey();
					int counter = entry.getValue();
					
					//EncryptedFingerprint item = repo.getEncryptedFingerprints().get(id);
					
					RawRecord resultRecord = rawDBRecords.get(id-1);
					
					int dist = Distance.getHammingDistanceV2(queryRecord.getValue(), resultRecord.getValue());
					
					if (dist > threshold) {
						
						continue;
					}
					
					System.out.println(id + " :: " + resultRecord.getName() + " :: " + resultRecord.getValue() + " >>> dist: " + dist + "  Counter::" + counter);	
					
				}
				
				//System.out.println("Avg dec time is:" + (double)avgDecTime/searchResult.size() / 1000000 + " ms.");
				
			} else {
				System.out.println("No similar item!!!");
			}
			
			// print the statistics
			//System.out.println("The recall is : " + MyAnalysis.computeRecall(queryRecord.getName(), searchResult, rawRecords, numOfPositive));
			
			//System.out.println("The precision is : " + MyAnalysis.computePrecision(queryRecord.getName(), searchResult, rawRecords));
		}
	}
	
	private static void analyzeTopK(IndexBuilder CP, IndexSearcher IS, List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, HammingLSH lsh, int threshold) {

		System.out.println("\nModel: analyze top-K.");
		
		RawRecord queryRecord;
		
		float avgGenTokenTime = 0;
		
		long avgSearchTime = 0;
						
		int avgNumOfCandidate = 0;
		
		int queryTimes = 0;
		
		int[] topK_list = {1,3,5,8,10,13,15,18,20,30,40,50}; // the choice of K for "top K"
		
		float[] avgAccuracy = new float[topK_list.length];
		
		for(int j=0; j<avgAccuracy.length;++j) {
			avgAccuracy[j] = 0.0F;
		}
		
		for (int i = 0; i < queryRecords.size(); i++) {
			
			queryRecord = queryRecords.get(i);
			
			++queryTimes;
			System.out.println(queryTimes);
			
			long stOfGenToken = System.currentTimeMillis();
			
			// prepare the query message
			GenTokenResult gtr = CP.genSearchToken(queryRecord, lsh, ++numOfSearch);
			
			long etOfGenToken = System.currentTimeMillis();
			
			long time1 = System.currentTimeMillis();
			
			SimSearchResult ssr = IS.simSearch(gtr.tokens);
			
			Map<Integer, Integer> searchResult = ssr.idMap;
			
			// rank the result
			List<Entry<Integer, Integer>> rankedList = new ArrayList<Entry<Integer, Integer>>(searchResult.entrySet());

			Collections.sort(rankedList, new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object e1, Object e2) {
					int v1 = ((Entry<Integer, Integer>) e1).getValue();
					int v2 = ((Entry<Integer, Integer>) e2).getValue();
					return v2 - v1;

				}
			});
			
			long time2 = System.currentTimeMillis();

			System.out.println("Cost " + (time2 - time1) + " ms.");
			
			CP.updateCRTAfterSearch(gtr.tHats, ssr.ncrtList);

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
	
	private static void evaluateSearchTime(IndexBuilder CP, IndexSearcher IS, List<RawRecord> queryRecords, List<RawRecord> rawDBRecords, HammingLSH lsh, int threshold) {

		System.out.println("\nModel: evaluate search time.");
		
		RawRecord queryRecord;
		
		float avgGenTokenTime = 0;
		
		long avgSearchTime = 0;
						
		int avgNumOfCandidate = 0;
		
		int queryTimes = 0;
		
		int[] topK_list = {1,3,5,8,10,13,15,18,20,30,40,50}; // the choice of K for "top K"
		
		float[] avgAccuracy = new float[topK_list.length];
		
		for(int j=0; j<avgAccuracy.length;++j) {
			avgAccuracy[j] = 0.0F;
		}
		
		for (int i = 0; i < queryRecords.size(); i++) {
			
			queryRecord = queryRecords.get(i);
			
			++queryTimes;
			System.out.println(queryTimes);
			
			long stOfGenToken = System.currentTimeMillis();
			
			// prepare the query message
			GenTokenResult gtr = CP.genSearchToken(queryRecord, lsh, ++numOfSearch);
			
			long etOfGenToken = System.currentTimeMillis();
			
			long time1 = System.currentTimeMillis();
			
			SimSearchResult ssr = IS.simSearch(gtr.tokens);
			
			Map<Integer, Integer> searchResult = ssr.idMap;
			
			// rank the result
			List<Entry<Integer, Integer>> rankedList = new ArrayList<Entry<Integer, Integer>>(searchResult.entrySet());

			Collections.sort(rankedList, new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object e1, Object e2) {
					int v1 = ((Entry<Integer, Integer>) e1).getValue();
					int v2 = ((Entry<Integer, Integer>) e2).getValue();
					return v2 - v1;

				}
			});
			
			long time2 = System.currentTimeMillis();

			System.out.println("Cost " + (time2 - time1) + " ms.");
			
			CP.updateCRTAfterSearch(gtr.tHats, ssr.ncrtList);

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
	
	private static void encryptFP(List<RawRecord> rawRecords, Map<Integer, EncryptedFingerprint> encryptedFingerprints, HashElGamalParameters params, BigInteger pk) {

		//Map<Integer, EncryptedFingerprint> encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>();
		
		for (int i = 0; i < rawRecords.size(); i++) {
			
			// encrypt fingerprint
			HashElGamalCiphertext cipherFP = HashElGamal.encrypt(params, pk, rawRecords.get(i).getValue());
			
			encryptedFingerprints.put(rawRecords.get(i).getId(),
					new EncryptedFingerprint(rawRecords.get(i).getName(), cipherFP));
		}
	}
}
