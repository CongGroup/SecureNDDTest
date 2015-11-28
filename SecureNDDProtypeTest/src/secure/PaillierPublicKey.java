package secure;

import java.math.BigInteger;

public class PaillierPublicKey {

	private BigInteger n;
	
	private BigInteger g;
	
	private BigInteger nsquare;
	
	private int bitLength;
	
	public PaillierPublicKey(BigInteger n, BigInteger g, BigInteger nsquare, int bitLength) {
		
		this.n = n;
		this.g = g;
		this.nsquare = nsquare;
		this.bitLength = bitLength;
	}
	
	public PaillierPublicKey(PaillierPublicKey pk) {
		
		this.n = pk.n;
		this.g = pk.g;
		this.nsquare = pk.nsquare;
		this.bitLength = pk.bitLength;
	}

	public BigInteger getN() {
		return n;
	}

	public void setN(BigInteger n) {
		this.n = n;
	}

	public BigInteger getG() {
		return g;
	}

	public void setG(BigInteger g) {
		this.g = g;
	}

	public BigInteger getNsquare() {
		return nsquare;
	}

	public void setNsquare(BigInteger nsquare) {
		this.nsquare = nsquare;
	}

	public int getBitLength() {
		return bitLength;
	}

	public void setBitLength(int bitLength) {
		this.bitLength = bitLength;
	}
}
