package base;

import java.math.BigInteger;

import secure.HashElGamal;
import secure.HashElGamalKeyPair;
import secure.HashElGamalParameters;

//import secure.Paillier;
//import secure.PaillierPrivateKey;
//import secure.PaillierPublicKey;

/**
 * This party is used to simulate the GC operations.
 * 
 * @author Helei
 *
 */
public class ThirdParty {
	
	private BigInteger keyPublic;
	
	private BigInteger keyPrivate;

	public ThirdParty(Parameters params) {
		
		// generate keyF for fingerprint encryption and its corresponding
		// private key
//		Paillier paillier = new Paillier(params.bitLength, params.certainty);
//
//		this.keyPublic = new PaillierPublicKey(paillier.getN(), paillier.getG(), paillier.getNsquare(),
//				params.bitLength);
//
//		BigInteger u = paillier.getG().modPow(paillier.getLambda(), paillier.getNsquare()).subtract(BigInteger.ONE)
//				.divide(paillier.getN()).modInverse(paillier.getN());
//
//		this.keyPrivate = new PaillierPrivateKey(paillier.getLambda(), u);
		
		HashElGamalKeyPair keyPair = HashElGamal.genKeyPair(params.hashElGamalPara);

		this.keyPublic = keyPair.getPk();

		this.keyPrivate = keyPair.getSk();
	}
	
	public ThirdParty(HashElGamalParameters hashElGamalPara) {
		
		// generate keyF for fingerprint encryption and its corresponding
		// private key
		
		HashElGamalKeyPair keyPair = HashElGamal.genKeyPair(hashElGamalPara);

		this.keyPublic = keyPair.getPk();

		this.keyPrivate = keyPair.getSk();
	}

	public BigInteger getKeyPublic() {
		return keyPublic;
	}

	public void setKeyPublic(BigInteger keyPublic) {
		this.keyPublic = keyPublic;
	}

	public BigInteger getKeyPrivate() {
		return keyPrivate;
	}

	public void setKeyPrivate(BigInteger keyPrivate) {
		this.keyPrivate = keyPrivate;
	}
}
