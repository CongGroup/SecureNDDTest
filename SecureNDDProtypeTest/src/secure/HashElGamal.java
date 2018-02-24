package secure;

import java.math.BigInteger;

import secure.HashElGamalCiphertext;
import secure.HashElGamalKeyPair;
import secure.HashElGamalParameters;

public class HashElGamal {

	public static HashElGamalKeyPair genKeyPair(HashElGamalParameters param) {

		BigInteger sk = BigInteger.valueOf(param.rand.nextLong()).mod(param.biP);

		BigInteger pk = param.biG.modPow(sk, param.biP);

		return new HashElGamalKeyPair(sk, pk);
	}

	public static HashElGamalCiphertext encrypt(HashElGamalParameters param, BigInteger pk, BigInteger msg) {

		BigInteger r = (BigInteger.valueOf(param.rand.nextLong())).mod(param.biP);

		BigInteger u = param.biG.modPow(r, param.biP);

		BigInteger v = BigInteger.valueOf(PRF.SHA256ToUnsignedInt(pk.modPow(r, param.biP).toString())).xor(msg);

		return new HashElGamalCiphertext(u, v);
	}

	public static BigInteger decrypt(HashElGamalParameters param, BigInteger sk, HashElGamalCiphertext ciphertext) {

		return BigInteger.valueOf(PRF.SHA256ToUnsignedInt(ciphertext.getU().modPow(sk, param.biP).toString())).xor(ciphertext.getV());
	}

	public static HashElGamalCiphertext addMask(HashElGamalCiphertext c, BigInteger mask) {

		return new HashElGamalCiphertext(c.getU(), c.getV().xor(mask));
	}
}