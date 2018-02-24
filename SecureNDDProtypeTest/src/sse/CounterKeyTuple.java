package sse;

import java.io.Serializable;

public class CounterKeyTuple implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3791498261532020832L;
	
	private Long crt;
	private Long ucrt;
	private String k;
	private String ku;

	public CounterKeyTuple(Long crt, Long ucrt, String k, String ku) {
		super();
		this.crt = crt;
		this.ucrt = ucrt;
		this.k = k;
		this.ku = ku;
	}

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

	public String getKu() {
		return ku;
	}

	public void setKu(String ku) {
		this.ku = ku;
	}

	public Long getCrt() {
		return crt;
	}

	public void setCrt(Long crt) {
		this.crt = crt;
	}

	public Long getUcrt() {
		return ucrt;
	}

	public void setUcrt(Long ucrt) {
		this.ucrt = ucrt;
	}
}
