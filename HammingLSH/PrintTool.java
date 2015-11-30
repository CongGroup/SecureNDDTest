package util;

public class PrintTool {

	public static final short OUT = 1;
	
	public static final short ERROR = 2;
	
	public static final short WARNING = 3;
	
	public static void print(short type, String msg) {
		
		switch (type) {
		case OUT:
			System.out.print(msg);
			break;
		case ERROR:
			System.err.print("ERROR: " + msg);
			break;
		case WARNING:
			System.out.print("WARNING: " + msg);
		default:
			break;
		}
	}
	
	public static void println(short type, String msg) {
		
		PrintTool.print(type, msg + "\n");
	}
	
	public static void println(short type, String prefix, String msg) {
		
		System.out.print(prefix);
		
		PrintTool.println(type, msg);
	}
	
	public static void println(short type, String prefix, String msg, String suffix) {
		
		System.out.print(prefix);
		
		PrintTool.println(type, msg + suffix);
	}
	
	public static void printArray(long[] vector) {
		
		System.out.print("[");
		
		for (int i = 0; i < vector.length; i++) {
			
			System.out.print(vector[i]);
			
			if (i != vector.length - 1) {
				System.out.print(", ");
			} else {
				System.out.println("]");
			}
		}
	}
}
