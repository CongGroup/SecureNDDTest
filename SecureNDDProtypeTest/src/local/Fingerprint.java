package local;

import java.math.BigInteger;

public class Fingerprint {

	private int id;
	private String type;
	private int length;
	private byte[] raw;
	private BigInteger value;
	
	public Fingerprint() {
		super();
	}
	
	public Fingerprint(int id, String type, int length, byte[] raw) {
		super();
		this.id = id;
		this.type = type;
		this.length = length;
		this.raw = raw;
	}

	public Fingerprint(int id, String type, int length, byte[] raw,
			BigInteger value) {
		super();
		this.id = id;
		this.type = type;
		this.length = length;
		this.raw = raw;
		this.value = value;
	}
	
	public void genValue() {
		
		if (this.value == null) {
			
			this.value = new BigInteger(1, this.raw);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getRaw() {
		return raw;
	}

	public void setRaw(byte[] raw) {
		this.raw = raw;
	}

	public BigInteger getValue() {
		return value;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}
}
