package base;

import java.math.BigInteger;

public class EncryptedFingerprint {

	private String name;
	
	private BigInteger cipherFP;
	
	public EncryptedFingerprint(String name, BigInteger cipherFP) {
		
		this.name = name;
		this.cipherFP = cipherFP;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigInteger getCipherFP() {
		return cipherFP;
	}

	public void setCipherFP(BigInteger cipherFP) {
		this.cipherFP = cipherFP;
	}
}
