package sse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimSearchResult {

	public Map<Integer, Integer> idMap;
	public List<Long> ncrtList;
	
	public SimSearchResult() {
		this.idMap = new ConcurrentHashMap<>();
		this.ncrtList = new ArrayList<>();
	}
}
