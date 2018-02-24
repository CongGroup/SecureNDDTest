package sse;

public class TokenTuple {

	public K1K2Pair t;
	public long crt;
	public K1K2Pair tu;
	public long ucrt;
	public K1K2Pair nt;
	
	public TokenTuple(K1K2Pair t, long crt, K1K2Pair tu, long ucrt, K1K2Pair nt) {
		
		this.t = t;
		this.crt = crt;
		this.tu = tu;
		this.ucrt = ucrt;
		this.nt = nt;
	}
}
