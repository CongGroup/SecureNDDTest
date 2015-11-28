package local;

import java.math.BigInteger;

public class NameFingerprintPair {

	private String name;
	
	private BigInteger value;
	
	public NameFingerprintPair(String name, BigInteger value) {
		
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigInteger getValue() {
		return value;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}
}
