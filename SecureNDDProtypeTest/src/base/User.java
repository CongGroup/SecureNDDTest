package base;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
//import secure.PaillierPublicKey;

public class User {

	private int uid;
	
	private Element keyV;
	
	private BigInteger keyPublic;
	
	private Parameters params;

	public User(int uid, Parameters params, BigInteger keyPublic) {
		
		this.uid = uid;
		
		this.params = params;
		
		// generate keyV for multi-key search
		this.keyV = params.pairing.getZr().newRandomElement().getImmutable();

		this.keyPublic = keyPublic;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
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

	public Element getKeyVForAuthorization() {
		return params.g2.powZn(params.pairing.getZr().newOneElement().div(this.keyV)).getImmutable();
	}
}
