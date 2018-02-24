package secure;

import java.math.BigInteger;
import java.util.Random;

public class HashElGamalParameters {
	
	public Random rand;
	
	public BigInteger biG;
	
	public BigInteger biP;

	public HashElGamalParameters(int bitLength, int certainty) {
		
		this.rand = new Random();
		
		this.biG = new BigInteger("2");
		
		this.biP = new BigInteger(bitLength, certainty, rand);
	}
}
