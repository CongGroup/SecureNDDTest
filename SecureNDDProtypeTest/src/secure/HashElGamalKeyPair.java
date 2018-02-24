package secure;

import java.math.BigInteger;

public class HashElGamalKeyPair {

	private BigInteger sk;
	
	private BigInteger pk;
	
	public HashElGamalKeyPair(BigInteger sk, BigInteger pk) {
		
		this.sk = sk;
		this.pk = pk;
	}

	public BigInteger getSk() {
		return sk;
	}

	public void setSk(BigInteger sk) {
		this.sk = sk;
	}

	public BigInteger getPk() {
		return pk;
	}

	public void setPk(BigInteger pk) {
		this.pk = pk;
	}
}
