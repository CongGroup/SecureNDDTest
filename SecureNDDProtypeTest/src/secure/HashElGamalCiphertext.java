package secure;

import java.math.BigInteger;

public class HashElGamalCiphertext {
	
	private BigInteger u;
	
	private BigInteger v;
	
	public HashElGamalCiphertext(BigInteger u, BigInteger v) {
		
		this.u = u;
		this.v = v;
	}

	public BigInteger getU() {
		return u;
	}

	public void setU(BigInteger u) {
		this.u = u;
	}

	public BigInteger getV() {
		return v;
	}

	public void setV(BigInteger v) {
		this.v = v;
	}
}
