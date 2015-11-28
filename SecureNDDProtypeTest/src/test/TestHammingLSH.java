package test;

import java.math.BigInteger;

import util.PrintTool;
import base.HammingLSH;

public class TestHammingLSH {

	public static void main(String[] args) {
		
		HammingLSH lsh = new HammingLSH(64, 5, 10);
		
		int[] data = new int[64];
		
		int[] data2 = new int[64];
		data2[0] = 1;
		
		int[] data3 = new int[64];
		for (int i = 0; i < 10; i++) {
			data3[i] = 1;
		}
		
		long[] hash = lsh.computeLSH(data);
		
		long[] hash2 = lsh.computeLSH(data2);
		
		long[] hash3 = lsh.computeLSH(data3);
		
		PrintTool.printArray(hash);
		PrintTool.printArray(hash2);
		PrintTool.printArray(hash3);
		
		BigInteger b1 = BigInteger.valueOf(1022L);;
		
		long[] hash1 = lsh.computeLSH(b1);
		
		BigInteger b2 = BigInteger.valueOf(1023L);
		
		long[] hash22 = lsh.computeLSH(b2);
		
		PrintTool.printArray(hash);
		PrintTool.printArray(hash1);
		PrintTool.printArray(hash22);
		PrintTool.printArray(hash3);
	}
	
	
}
