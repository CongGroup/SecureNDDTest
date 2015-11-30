package base;

import java.math.BigInteger;

import util.Converter;

public class Distance {

	public static int getHammingDistance(String v1, String v2) {

		if (v1.length() != v2.length()) {
			return -1;
		}

		int counter = 0;

		for (int i = 0; i < v1.length(); i++) {
			if (v1.charAt(i) != v2.charAt(i))
				counter++;
		}

		return counter;
	}

	public static int getHammingDistanceV1(BigInteger v1, BigInteger v2) {

		int dist = 0;
		
		String sv1 = Converter.bigInteger2String(v1, 9);
		String sv2 = Converter.bigInteger2String(v2, 9);

		dist = getHammingDistance(sv1, sv2);

		return dist;
	}

	public static int getHammingDistanceV2(BigInteger v1, BigInteger v2) {

		int dist = v1.xor(v2).bitCount();

		return dist;
	}
}
