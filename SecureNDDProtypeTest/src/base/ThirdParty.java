package base;

import java.math.BigInteger;

import secure.Paillier;
import secure.PaillierPrivateKey;
import secure.PaillierPublicKey;

/**
 * This party is used to simulate the GC operations.
 * 
 * @author Helei
 *
 */
public class ThirdParty {
	
	private PaillierPublicKey keyPublic;
	
	private PaillierPrivateKey keyPrivate;

	public ThirdParty(Parameters params) {
		
		// generate keyF for fingerprint encryption and its corresponding
		// private key
		Paillier paillier = new Paillier(params.bitLength, params.certainty);

		this.keyPublic = new PaillierPublicKey(paillier.getN(), paillier.getG(), paillier.getNsquare(),
				params.bitLength);

		BigInteger u = paillier.getG().modPow(paillier.getLambda(), paillier.getNsquare()).subtract(BigInteger.ONE)
				.divide(paillier.getN()).modInverse(paillier.getN());

		this.keyPrivate = new PaillierPrivateKey(paillier.getLambda(), u);
	}

	public PaillierPublicKey getKeyPublic() {
		return keyPublic;
	}

	public void setKeyPublic(PaillierPublicKey keyPublic) {
		this.keyPublic = keyPublic;
	}

	public PaillierPrivateKey getKeyPrivate() {
		return keyPrivate;
	}

	public void setKeyPrivate(PaillierPrivateKey keyPrivate) {
		this.keyPrivate = keyPrivate;
	}
}
