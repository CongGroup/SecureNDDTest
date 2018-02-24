package sse;

import java.util.ArrayList;
import java.util.List;

public class SearchTokens {
	
	private int size = 0;

	public List<K1K2Pair> ts;
	public List<K1K2Pair> tus;
	public List<K1K2Pair> nts;
	public List<Integer>  crts;
	
	public SearchTokens() {
		
		this.ts          = new ArrayList<K1K2Pair>();
		this.tus         = new ArrayList<K1K2Pair>();
		this.nts         = new ArrayList<K1K2Pair>();
		this.crts        = new ArrayList<Integer>();
	}
	
	public void setSize(int curSize) {
		
		this.size = curSize;
	}
	
	public int getSize() {
		
		return this.size;
	}
}
