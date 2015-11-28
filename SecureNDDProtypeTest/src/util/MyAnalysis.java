package util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import base.Distance;
import base.RawRecord;

public class MyAnalysis {
	
	public static float computeRecall(String queryName, Map<Integer, Integer> searchResult, List<RawRecord> rawRecords, int numOfPositive) {
		
		int numOfTruePositive = 0;
		//int numOfPositive = 0;
		
		for (Map.Entry<Integer, Integer> entry : searchResult.entrySet()) {

			int id = entry.getKey();
			//int counter = entry.getValue();
			
			RawRecord rawRecord = rawRecords.get(id - 1);
			
			if (checkTruePositive(queryName, rawRecord.getName())) {
				
				numOfTruePositive++;
			}
		}
		
		return (float)numOfTruePositive/numOfPositive;
	}

	public static float computePrecision(String queryName, Map<Integer, Integer> searchResult, List<RawRecord> rawRecords) {
		
		int numOfTruePositive = 0;
		int numOfMatches = searchResult.size();
		
		for (Map.Entry<Integer, Integer> entry : searchResult.entrySet()) {

			int id = entry.getKey();
			//int counter = entry.getValue();
			
			RawRecord rawRecord = rawRecords.get(id - 1);
			
			if (checkTruePositive(queryName, rawRecord.getName())) {
				
				numOfTruePositive++;
			}
		}
		
		return (float)numOfTruePositive/numOfMatches;
	}
	
	public static boolean checkTruePositive(String queryName, String testName) {
		
		String name1 = queryName.substring(queryName.lastIndexOf("_"), queryName.lastIndexOf("."));
		
		String name2 = testName.substring(testName.lastIndexOf("_"), testName.lastIndexOf("."));
		
		return (name1.equals(name2));
	}
	
	public static float computeTopK(RawRecord query,
			List<Entry<Integer, Integer>> rankedList, List<RawRecord> rawRecords,
			int epsilon) {

		int numOfTruePositive = 0;
		// int numOfPositive = 0;
		
		for (Entry<Integer, Integer> entry : rankedList) {
		
			//System.out.print(query.getName() + " = " + rawRecords.get(entry.getKey()-1).getName() + " :: dist = ");
			if (checkDistance(query, rawRecords.get(entry.getKey()-1), epsilon)) {
				numOfTruePositive++;
			}
		}
		
		// in case rankedList is empty
		if(rankedList.size() == 0)
			return 1;

		return (float) numOfTruePositive/rankedList.size();
	}
	
	public static int computeCDF(RawRecord query,
			List<Entry<Integer, Integer>> rankedList, List<RawRecord> rawRecords,
			int epsilon, int numOfRetrieval) 
	{
		int numOfTruePositive = 0;
		
		int cntCompare = 0; // counter for comparing until finding numOfRetrieval TP results
		
		for (Entry<Integer, Integer> entry : rankedList) 
		{	
			if(numOfTruePositive == numOfRetrieval)
				break;
			
			if (checkDistance(query, rawRecords.get(entry.getKey()-1), epsilon)) 
			{
				numOfTruePositive++;
			}
			++cntCompare;
			
		}
		return cntCompare;
		
	}
	
	public static boolean checkDistance(RawRecord query, RawRecord candidate, int epsilon) {

		int dist = Distance.getHammingDistanceV2(query.getValue(), candidate.getValue());

		//System.out.println(dist);
		
		return (dist <= epsilon);
	}
}
