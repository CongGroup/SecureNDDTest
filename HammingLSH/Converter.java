package util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Converter {

	/**
	 * 
	 * @param bi
	 * @param length number of byte
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
		
		//System.out.println(sb.toString());
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
        buffer.flip();//need flip
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
     * @param data the original data
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
}
