package base;

import java.math.BigInteger;

import secure.HashElGamalCiphertext;

public class EncryptedFingerprint {

	private String name;
	
	private HashElGamalCiphertext cipherFP;
	
	public EncryptedFingerprint(String name, HashElGamalCiphertext cipherFP) {
		
		this.name = name;
		this.cipherFP = cipherFP;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashElGamalCiphertext getCipherFP() {
		return cipherFP;
	}

	public void setCipherFP(HashElGamalCiphertext cipherFP) {
		this.cipherFP = cipherFP;
	}
}
