package sse;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class MathTool {

	/**
	 * 
	 * @param bi
	 * @param length
	 *            number of byte
	 * @return
	 */
	public static String bigInteger2String(BigInteger bi, int length) {

		String ZERO = "00000000";
		byte[] data = bi.toByteArray();

		StringBuffer sb = new StringBuffer();

		if (data.length > length) {

			byte[] temp = new byte[length];
			System.arraycopy(data, data.length - length, temp, 0, length);

			data = temp;
		} else if (data.length < length) {
			int paddingNum = length - data.length;

			for (int i = 0; i < paddingNum; i++) {
				sb.append(ZERO);
			}
		}

		for (int i = 0; i < data.length; i++) {
			String s = Integer.toBinaryString(data[i]);
			if (s.length() > 8) {
				s = s.substring(s.length() - 8);
			} else if (s.length() < 8) {
				s = ZERO.substring(s.length()) + s;
			}

			sb.append(s);
		}

		// System.out.println(sb.toString());
		return sb.toString();
	}

	public static String bytes2Hex(byte[] bts) {

		StringBuilder des = new StringBuilder();
		String tmp;

		for (byte bt : bts) {

			tmp = (Integer.toHexString(bt & 0xFF));

			if (tmp.length() == 1) {

				des.append('0');
			}
			des.append(tmp.toUpperCase());
		}
		return des.toString();
	}

	private static ByteBuffer buffer = ByteBuffer.allocate(8);

	public static byte[] longToBytes(long x) {
		buffer.clear();
		buffer.putLong(0, x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {

		buffer.clear();
		buffer.put(bytes, 0, 8);
		buffer.flip();// need flip
		return buffer.getLong();
	}

	public static long bytesToUnsignedInt(byte[] bytes) {

		ByteBuffer buffer = ByteBuffer.allocate(8);

		buffer.clear();
		buffer.put(bytes, 0, 4);
		buffer.flip();
		return buffer.getInt() & 0x0FFFFFFFFl;
	}

	/**
	 * @param data
	 *            the original data
	 * @return long
	 */
	public static long flod256Bytes(byte[] data) {

		long result = 0;

		if (data.length == 32) {

			byte[][] bb = new byte[4][8];

			for (int i = 0; i < data.length; ++i) {

				bb[i / 8][i % 8] = data[i];
			}

			result = bytesToLong(bb[0]) ^ bytesToLong(bb[1]) ^ bytesToLong(bb[2]) ^ bytesToLong(bb[3]);
		}

		return result;
	}

	public static int mapIndex(int id, int loopSize) {

		if (id % loopSize == 0) {
			return loopSize - 1;
		} else {
			return id % loopSize - 1;
		}

	}

	public static long foldBytes2Long(byte[] data) {

		long result = 0;
		
		// TODO: double check this
		int length = data.length / 8 * 8;

		// ensure the length is 8X, for simplicity
		if (length % 8 == 0) {

			byte[][] bb = new byte[length / 8][8];

			for (int i = 0; i < length; ++i) {

				bb[i / 8][i % 8] = data[i];
			}

			result = bytesToLong(bb[0]);

			for (int i = 1; i < length / 8; ++i) {

				result ^= bytesToLong(bb[i]);
			}
		}

		return result;
	}

	public static int bytesToInt(byte[] bytes) {

		int val = 0;
		if (bytes.length > 4) {
			throw new RuntimeException("Too big to fit in int");
		}
		
		for (int i = 0; i < 4; i++) {
			val = val << 8;
			val = val | (bytes[i] & 0xFF);
		}
		return val;
	}
	
	public static int foldBytes2Int(byte[] data) {

		int result = 0;

		// ensure the length is 4X, for simplicity
		if (data.length % 4 == 0) {

			byte[][] bb = new byte[data.length / 4][4];

			for (int i = 0; i < data.length; ++i) {

				bb[i / 4][i % 4] = data[i];
			}
			
			result = bytesToInt(bb[0]);

			for (int i = 1; i < data.length / 4; ++ i) {
				
				result ^= bytesToInt(bb[i]);
			}
		}

		return result;
	}
	
	public static int encryptValue(byte[] key, int value) {
		
		int temp = foldBytes2Int(key);
		return temp ^ value;
	}
	
	public static int decryptValue(byte[] key, int cipher) {
		
		int temp = foldBytes2Int(key);
		return temp ^ cipher;
	}
}
