package local;

public abstract class DataItem {

	private int id;
	
	private String name;
	
	private Fingerprint fingerprint;
	
	public abstract void generate(int length, int type);
	
	public DataItem(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public DataItem(int id, String name, Fingerprint fingerprint) {
		super();
		this.id = id;
		this.name = name;
		this.fingerprint = fingerprint;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(Fingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
