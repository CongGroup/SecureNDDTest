package base;


public class SecureToken {

	private String r;
	
	private long h;

	public SecureToken(String r, long h) {
		
		this.r = r;
		this.h = h;
	}

	public String getR() {
		return r;
	}

	public void setR(String r) {
		this.r = r;
	}

	public long getH() {
		return h;
	}

	public void setH(long h) {
		this.h = h;
	}
}
