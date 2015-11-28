package base;

import java.math.BigInteger;

public class RawRecord {

	private int id;
	
	private String name;
	
	private BigInteger value;
	
	public RawRecord(int id, String name, BigInteger value) {
		
		this.id = id;
		this.name = name;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
