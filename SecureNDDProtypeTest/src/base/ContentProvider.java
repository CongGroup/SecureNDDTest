package base;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
//import secure.PaillierPublicKey;

public class ContentProvider {

	private int cpid;
	
	private Element keyV;
	
	private BigInteger keyPublic;
	
	private Parameters params;

	public ContentProvider(int cpid, Parameters params, BigInteger keyPublic) {
		
		this.cpid = cpid;
		
		this.params = params;
		
		// generate keyV for multi-key search
		this.keyV = params.pairing.getZr().newRandomElement().getImmutable();

		this.keyPublic = keyPublic;
	}
	
	/**
	 * Content provider gives user[uid] the access right.
	 * 
	 * @return
	 */
	public Element authorize(Element userKeyForAuth) {
		
		return  userKeyForAuth.powZn(getKeyV());//params.g2.powZn(getKeyV().mul(userKeyV));
	}

	public int getCpid() {
		return cpid;
	}

	public void setCpid(int cpid) {
		this.cpid = cpid;
	}

	public Element getKeyV() {
		return keyV;
	}

	public void setKeyV(Element keyV) {
		this.keyV = keyV;
	}

	public BigInteger getKeyPublic() {
		return keyPublic;
	}

	public void setKeyPublic(BigInteger keyPublic) {
		this.keyPublic = keyPublic;
	}

	public Parameters getParams() {
		return params;
	}

	public void setParams(Parameters params) {
		this.params = params;
	}
}
