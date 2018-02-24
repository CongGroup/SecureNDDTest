package sse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenTokenResult {

	public List<TokenTuple> tokens;
	
	public List<String> tHats;
	
//	// <l>
//	public List<Integer> lList;
//	
//	// <l, tHat>
//	public Map<Integer, String> tHatMap;

	public GenTokenResult() {
		
		this.tokens = new ArrayList<>();
		this.tHats  = new ArrayList<>();
//		this.lList = new ArrayList<>();
//		this.tHatMap = new HashMap<>();
	}
}
