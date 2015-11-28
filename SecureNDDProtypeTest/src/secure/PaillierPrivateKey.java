package secure;

import java.math.BigInteger;

public class PaillierPrivateKey {

	private BigInteger lambda;
	
	private BigInteger u;
	
	public PaillierPrivateKey(BigInteger lambda, BigInteger u) {
		
		this.lambda = lambda;
		this.u = u;
	}

	public BigInteger getLambda() {
		return lambda;
	}

	public void setLambda(BigInteger lambda) {
		this.lambda = lambda;
	}

	public BigInteger getU() {
		return u;
	}

	public void setU(BigInteger u) {
		this.u = u;
	}
}
