package sse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import sse.SearchTokens;
import sse.SimSearchResult;
import sse.TokenTuple;
import secure.PRF;
import sse.FileTool;
import sse.MathTool;

public class IndexSearcher {

	private int nodeId;
	
	private Map<String, String> mapIndex;// = new ConcurrentHashMap<>();

	public IndexSearcher(int nodeId) {
		super();
		this.nodeId = nodeId;
	}
	
	public void loadIndex(String indexFilePath) {
		
		mapIndex = FileTool.readFromFile(indexFilePath);
	}
	
	public void loadIndex(IndexBuilder indexBuilder) {
		
		mapIndex = indexBuilder.getMapIndex();
	}
	
	public SimSearchResult simSearch(List<TokenTuple> tokens) {
		
		SimSearchResult ssr = new SimSearchResult();
		
		List<Integer> tempResults = new ArrayList<>();
		
		for (int i = 0; i < tokens.size(); ++i) {
			
			String strK1  = tokens.get(i).t.strK1;
			String strK2  = tokens.get(i).t.strK2;
			long   crt    = tokens.get(i).crt;
			
			String strUK1 = tokens.get(i).tu.strK1;
			String strUK2 = tokens.get(i).tu.strK2;
			long   ucrt   = tokens.get(i).ucrt;
			
			String strNK1 = tokens.get(i).nt.strK1;
			String strNK2 = tokens.get(i).nt.strK2;
			
			long ncrt = 0L;
			
			// TODO: double check the boundary of c
			for (long c = 1; c <= crt; ++c) {
				String strK4 = PRF.F4(strK1, String.valueOf(c));
				
				if (mapIndex.containsKey(strK4)) {
					
					String strK3AndCipher = mapIndex.get(strK4);
					StringTokenizer st = new StringTokenizer(strK3AndCipher, "::");
					String strK3 = st.nextToken();
					int cipher = Integer.parseInt(st.nextToken());
					
					int id = MathTool.decryptValue(strK2.getBytes(), cipher);
					tempResults.add(id);
					
					mapIndex.remove(strK3);
					mapIndex.remove(strK4);
					
					++ncrt;
					
					String strNK4 = PRF.F4(strNK1, String.valueOf(ncrt));
					int newCipher = MathTool.encryptValue(strNK2.getBytes(), id);
					
					mapIndex.put(strK3, strNK4);
					mapIndex.put(strNK4, strK3 + "::" + newCipher);
				}
			}
			
			// TODO: double check the boundary of c
			for (long c = 1; c <= ucrt; ++c) {
				String strUK4 = PRF.F4(strUK1, String.valueOf(c));

				if (mapIndex.containsKey(strUK4)) {

					String strK3AndCipher = mapIndex.get(strUK4);
					StringTokenizer st = new StringTokenizer(strK3AndCipher, "::");
					String strK3 = st.nextToken();
					int cipher = Integer.parseInt(st.nextToken());

					int id = MathTool.decryptValue(strUK2.getBytes(), cipher);
					tempResults.add(id);
					

					mapIndex.remove(strK3);
					mapIndex.remove(strUK4);

					++ncrt;

					String strNK4 = PRF.F4(strNK1, String.valueOf(ncrt));
					int newCipher = MathTool.encryptValue(strNK2.getBytes(), id);

					mapIndex.put(strK3, strNK4);
					mapIndex.put(strNK4, strK3 + "::" + newCipher);
				}
			}
			
			
			
			ssr.ncrtList.add(ncrt);
		}
		
		for (int i = 0; i < tempResults.size(); ++i) {
			
			int id = tempResults.get(i);
			
			if (ssr.idMap.containsKey(id)) {
				
				ssr.idMap.put(id, ssr.idMap.get(id) + 1);
			} else {
				ssr.idMap.put(id, 1);
			}
		}
		
		return ssr;
	}
	
	public void dynamicAdd(Map<String, String> mapLocalIndex) {
		
		System.out.println(mapIndex.size());
		this.mapIndex.putAll(mapLocalIndex);
		System.out.println(mapIndex.size());
	}

	public void dynamicDel(List<String> delTokenList) {
		
		System.out.println(mapIndex.size());
		
		for (int i = 0; i < delTokenList.size(); ++i) {
			
			int c = 1;
			
			String strK3 = PRF.F3(delTokenList.get(i), String.valueOf(c));
			
			while (mapIndex.containsKey(strK3)) {
				
				String delValue = mapIndex.get(strK3);
				
				mapIndex.remove(strK3);
				mapIndex.remove(delValue);
				
				++c;
				strK3 = PRF.F3(delTokenList.get(i), String.valueOf(c));
			}
		}
		System.out.println(mapIndex.size());
	}
}
