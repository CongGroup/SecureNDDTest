package base;

import java.util.List;

public class SecureRecord {

	private int id;
	
	private List<SecureToken> secureTokens;
	
	public SecureRecord() {
		
	}
	
	public SecureRecord(int id, List<SecureToken> secureTokens) {
		
		this.id = id;
		this.secureTokens = secureTokens;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<SecureToken> getSecureTokens() {
		return secureTokens;
	}

	public void setSecureTokens(List<SecureToken> secureTokens) {
		this.secureTokens = secureTokens;
	}
	
	
}
