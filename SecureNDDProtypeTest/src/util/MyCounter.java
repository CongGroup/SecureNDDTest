package util;

public class MyCounter {

	private Long ctr;
	
	public MyCounter() {
		
		ctr = 0L;
	}

	public Long getCtr() {
		return ctr;
	}

	public void setCtr(Long ctr) {
		this.ctr = ctr;
	}
	
	public void addOne() {
		this.ctr++;
	}
}
