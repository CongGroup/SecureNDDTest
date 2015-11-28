package base;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;
import java.util.Map;

import secure.PRF;

public class SingleRepoInsertThread extends Thread {

	private MyCountDown threadCounter;

	//private Repository repo;
	private Element keyV;

	private Parameters params;

	private Map<Integer, SecureToken> secureTokensInL;

	private Map<Integer, Long> lshValuesInL;

	public SingleRepoInsertThread(String threadName, MyCountDown threadCounter,
			Element keyV, Map<Integer, Long> lshValuesInL,
			Map<Integer, SecureToken> secureTokensInL, Parameters params) {

		super(threadName);

		this.params = new Parameters(params);
		this.threadCounter = threadCounter;

		this.keyV = keyV;
		this.lshValuesInL = lshValuesInL;
		this.secureTokensInL = secureTokensInL;
	}

	public void run() {

		System.out.println(getName() + " is running!");

		for (Map.Entry<Integer, Long> entry : lshValuesInL.entrySet()) {

			Integer rdId = entry.getKey();
			long lshValue = entry.getValue();

			// Step 1: encrypt each LSH value
			Element r = params.pairing.getGT().newRandomElement()
					.getImmutable();

			String strR = r.toString();

			// System.out.println(strR);

			// pairing + H1()
			String t = (params.pairing.pairing(
					params.h1Pre.pow(BigInteger.valueOf(lshValue)), params.g2))
					.powZn(this.keyV).toString();

			// System.out.println(t);

			// H2()
			long h = PRF.HMACSHA1ToUnsignedInt(t, strR);

			// System.out.println("c = " + c);

			SecureToken seT = new SecureToken(strR, h);

			// Step 2: insert to the dict

			this.secureTokensInL.put(rdId, seT);
		}

		System.out.println(getName() + " is finished!");

		threadCounter.countDown();
	}
}
