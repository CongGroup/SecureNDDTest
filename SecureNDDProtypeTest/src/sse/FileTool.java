package sse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import base.RawRecord;
import local.DataItem;
import local.NameFingerprintPair;
import util.PrintTool;

public class FileTool {
	
	
	public static <E> void writeToFile(String filePath, E structureData) {

		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(structureData);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in " + filePath);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	


	@SuppressWarnings("unchecked")
	public static <E> E readFromFile(String filePath) {

		E structureData = null;

		try {
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			structureData = (E) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}

		System.out.println("Deserialized data is from " + filePath);
		return structureData;
	}
	

	/**
	 * 
	 * Format:
	 * 
	 * id<int>::fileName<String>::fingerprint<long>
	 * 
	 * or
	 * 
	 * id<4 byte>fileName<String>fingerprint<8 byte>
	 * 
	 * E.g.,
	 * 
	 * 1357::65535
	 * 
	 * @param outPath
	 * @param fileName
	 * @param docs
	 * @param type
	 *            boolean: true - binary format; false - plain format
	 */
	public static void writeFingerprint2File(String outPath, String fileName,
			List<DataItem> docs, boolean type) {

		String filePath = outPath + fileName;

		if (docs == null || docs.isEmpty()) {

			PrintTool.println(PrintTool.WARNING, "input files are empty!");
		} else {

			// type == true : binary format
			// type == false : plain format
			if (type) {

				File file = new File(filePath);

				if (!file.getParentFile().exists()) {

					file.getParentFile().mkdirs();
				}

				DataOutputStream dos = null;

				try {

					dos = new DataOutputStream(new FileOutputStream(file));

					for (int i = 0; i < docs.size(); i++) {

						DataItem tmp = docs.get(i);

						dos.writeInt(tmp.getId());
						dos.write(tmp.getFingerprint().getRaw());
					}

				} catch (IOException e) {

					e.printStackTrace();

				} finally {
					try {

						if (dos != null) {
							dos.close();
						}

					} catch (final IOException e) {

						PrintTool.println(PrintTool.ERROR,
								"fail to wrtie to file!");
					}
				}
			} else {

				BufferedWriter bw = null;

				try {

					bw = new BufferedWriter(new FileWriter(filePath, false));

					for (int i = 0; i < docs.size(); i++) {

						DataItem tmp = docs.get(i);

						bw.write(tmp.getId() + "::" + tmp.getName() + "::"
								+ tmp.getFingerprint().getValue() + "\n");
					}

				} catch (IOException e) {

					e.printStackTrace();
				} finally {

					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e1) {

							PrintTool.println(PrintTool.ERROR,
									"fail to wrtie to file!");
						}
					}
				}
			}
		}
	}

	/**
	 * type == true : binary format type == false : plain format
	 * [int]::[String]::[BigInteger]
	 * 
	 * @param inPath
	 * @param fileName
	 * @param type
	 * @return
	 */
	public static Map<Integer, NameFingerprintPair> readFingerprintFromFile(
			String inPath, String fileName, boolean type) {

		Map<Integer, NameFingerprintPair> result = new HashMap<Integer, NameFingerprintPair>();

		// TODO: read from binary case
		if (type) {

		} else {

			InputStreamReader reader = null;
			BufferedReader br = null;

			try {

				reader = new InputStreamReader(new FileInputStream(inPath
						+ fileName));

				br = new BufferedReader(reader);

				int numOfItem = 0;

				String line = br.readLine();

				while (line != null) {

					++numOfItem;

					StringTokenizer st = new StringTokenizer(line.replace("\n",
							""), "::");

					int id = Integer.valueOf(st.nextToken());
					String name = st.nextToken();
					BigInteger value = new BigInteger(st.nextToken());

					result.put(id, new NameFingerprintPair(name, value));

					line = br.readLine();
				}

				PrintTool.println(PrintTool.OUT, "Successfully read "
						+ numOfItem + " items from " + inPath + fileName);

			} catch (IOException e) {

				e.printStackTrace();

			} finally {
				try {

					if (reader != null) {
						reader.close();
					}

					if (br != null) {
						br.close();
					}

				} catch (final IOException e) {
					PrintTool
							.println(PrintTool.ERROR, "fail to read the file!");
				}
			}
		}

		return result;
	}

	public static List<NameFingerprintPair> readFingerprintFromFile2List(
			String inPath, String fileName, boolean type) {

		List<NameFingerprintPair> results = new ArrayList<NameFingerprintPair>();

		// TODO: read from binary case
		if (type) {

		} else {

			InputStreamReader reader = null;
			BufferedReader br = null;

			try {

				reader = new InputStreamReader(new FileInputStream(inPath
						+ fileName));

				br = new BufferedReader(reader);

				String line = br.readLine();

				while (line != null) {

					StringTokenizer st = new StringTokenizer(line.replace("\n",
							""), "::");

					//int id = Integer.valueOf(st.nextToken());
					st.nextToken();
					String name = st.nextToken();
					BigInteger value = new BigInteger(st.nextToken());

					results.add(new NameFingerprintPair(name, value));

					line = br.readLine();
				}

				PrintTool.println(PrintTool.OUT, "Successfully read "
						+ results.size() + " items from " + inPath + fileName);

			} catch (IOException e) {

				e.printStackTrace();

			} finally {
				try {

					if (reader != null) {
						reader.close();
					}

					if (br != null) {
						br.close();
					}

				} catch (final IOException e) {
					PrintTool
							.println(PrintTool.ERROR, "fail to read the file!");
				}
			}
		}

		return results;
	}
	
	public static List<RawRecord> readFingerprintFromFile2ListV2(
			String filePath, int numOfLimit, boolean type) {

		List<RawRecord> results = new ArrayList<RawRecord>();

		// TODO: read from binary case
		if (type) {

		} else {

			InputStreamReader reader = null;
			BufferedReader br = null;

			try {

				reader = new InputStreamReader(new FileInputStream(filePath));

				br = new BufferedReader(reader);

				int numOfLine = 0;
				
				String line = br.readLine();

				while (line != null) {

					numOfLine++;
					
					if (numOfLimit < numOfLine) {
						break;
					}
					
					StringTokenizer st = new StringTokenizer(line.replace("\n",
							""), "::");

					int id = Integer.valueOf(st.nextToken());
					String name = st.nextToken();
					BigInteger value = new BigInteger(st.nextToken());

					results.add(new RawRecord(id, name, value));

					line = br.readLine();
				}

				PrintTool.println(PrintTool.OUT, "Successfully read "
						+ results.size() + " items from " + filePath);

			} catch (IOException e) {

				e.printStackTrace();

			} finally {
				try {

					if (reader != null) {
						reader.close();
					}

					if (br != null) {
						br.close();
					}

				} catch (final IOException e) {
					PrintTool
							.println(PrintTool.ERROR, "fail to read the file!");
				}
			}
		}

		return results;
	}
	
	public static Map<Integer, NameFingerprintPair> readFingerprintFromFile2Map(
			String inPath, String fileName, boolean type) {

		Map<Integer, NameFingerprintPair> results = new HashMap<Integer, NameFingerprintPair>();

		// TODO: read from binary case
		if (type) {

		} else {

			InputStreamReader reader = null;
			BufferedReader br = null;

			try {

				reader = new InputStreamReader(new FileInputStream(inPath
						+ fileName));

				br = new BufferedReader(reader);

				String line = br.readLine();

				while (line != null) {

					StringTokenizer st = new StringTokenizer(line.replace("\n",
							""), "::");

					int id = Integer.valueOf(st.nextToken());
					String name = st.nextToken();
					BigInteger value = new BigInteger(st.nextToken());

					results.put(id, new NameFingerprintPair(name, value));

					line = br.readLine();
				}

				PrintTool.println(PrintTool.OUT, "Successfully read "
						+ results.size() + " items from " + inPath + fileName);

			} catch (IOException e) {

				e.printStackTrace();

			} finally {
				try {

					if (reader != null) {
						reader.close();
					}

					if (br != null) {
						br.close();
					}

				} catch (final IOException e) {
					PrintTool
							.println(PrintTool.ERROR, "fail to read the file!");
				}
			}
		}

		return results;
	}

	public static List<String> readLinesFromFile(String inPath, String fileName) {

		List<String> results = new ArrayList<String>();

		InputStreamReader reader = null;
		BufferedReader br = null;

		try {

			reader = new InputStreamReader(new FileInputStream(inPath
					+ fileName));

			br = new BufferedReader(reader);

			String line = br.readLine();

			while (line != null) {
				results.add(line.replace("\n", ""));

				line = br.readLine();
			}

			PrintTool.println(PrintTool.OUT, "Successfully read " + results.size()
					+ " lines from " + inPath + fileName);

		} catch (IOException e) {

			e.printStackTrace();

		} finally {
			try {

				if (reader != null) {
					reader.close();
				}

				if (br != null) {
					br.close();
				}

			} catch (final IOException e) {
				PrintTool.println(PrintTool.ERROR, "fail to read the file!");
			}
		}

		return results;
	}
}
