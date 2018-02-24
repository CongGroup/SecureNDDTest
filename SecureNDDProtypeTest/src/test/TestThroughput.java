package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import base.ContentProvider;
import base.EncryptedFingerprint;
import base.HammingLSH;
import base.MyCountDown;
import base.Parameters;
import base.RawRecord;
import base.Repository;
import base.SecureRecord;
import base.SecureToken;
import base.SysConstant;
import base.ThirdParty;
import base.User;
import it.unisa.dia.gas.jpbc.Element;
import secure.HashElGamal;
import secure.HashElGamalCiphertext;
import secure.PRF;
//import secure.Paillier;
import throughput.RepositoryForThroughputTest;
import throughput.ThroughputTestThread;
import util.ConfigParser;
import util.FileTool;
import util.MyCounter;
import util.PrintTool;

/**
 * The throughput test is slightly different from the rest of testing one. It mainly focuses on the throughput.
 * 
 * The thread is in "user" level.
 * @author Helei Cui
 * 
 */
public class TestThroughput {

	public static void main(String[] args) {

		if (args.length < 1) {

			PrintTool.println(PrintTool.ERROR,
					"please check the argument list!");

			return;
		}

		ConfigParser config = new ConfigParser(args[0]);

		String inputPath = config.getString("inputPath").replace("\\", "/");
		String rawRecordFileName = config.getString("dbFileName");
		String queryFileName = config.getString("queryFileName");

		int numOfLimit = config.getInt("numOfLimit");
		String pairingSettingPath = config.getString("pairingSettingPath");
		
		int bitLength = config.getInt("bitLength");
		int certainty = config.getInt("certainty");
		
		int lshL = config.getInt("lshL");
		int lshDimension = config.getInt("lshDimension");
		int lshK = config.getInt("lshK");
		
		// Step 1: preprocess: setup keys and read file
		Parameters params = new Parameters(pairingSettingPath, lshL, lshDimension, lshK, bitLength, certainty);
		
		HammingLSH lsh = new HammingLSH(lshDimension, lshL, lshK);
		
		System.out.println(">>> System parameters have been initialized");
		System.out.println(">>> Now, reading the raw test data from " + inputPath + rawRecordFileName);
		
		List<RawRecord> rawRecords = FileTool.readFingerprintFromFile2ListV2(inputPath, rawRecordFileName, numOfLimit, false);
		
		List<RawRecord> queryRecords = FileTool.readFingerprintFromFile2ListV2(inputPath, queryFileName, numOfLimit, false);

		if (numOfLimit > rawRecords.size()) {
			numOfLimit = rawRecords.size();
		}
		

		// Step 2: initialize the repositories and secure insert records
		System.out.println(">>> There are " + numOfLimit + " records.");
		
		// initialize a third party to simulate GC operations in this prototype testing.
		ThirdParty thirdParty = new ThirdParty(params);
				
		// initialize a content provider with cpid 1.
		ContentProvider contentProvider = new ContentProvider(1, params, thirdParty.getKeyPublic());
				
		// the repository id is 1.
		int rid = 1;
		
		RepositoryForThroughputTest repo = new RepositoryForThroughputTest(rid, params, contentProvider.getKeyV(), contentProvider.getKeyPublic());
		
		System.out.println(">>> Now, start inserting data into repositories...");
		
		long startTimeOfInsert = System.currentTimeMillis();
		
		// Compute LSH
		List<List<Long>> lshVectors = computeLSH(rawRecords, params);
		System.out.println(">> LSH converted.");
		
		// Encrypt fingerprints
		encryptFP(rawRecords, params, repo);
		System.out.println(">> fingerprints encrypted.");
		

		for (int i = 0; i < lshVectors.size(); i++) {
			
			List<SecureToken> secureTokens = new ArrayList<SecureToken>(lshL);
			
			for (int j = 0; j < lshL; j++) {
				
				long lshValue = lshVectors.get(i).get(j);

				// Step 1: encrypt each LSH value
				Element r = params.pairing.getGT().newRandomElement()
						.getImmutable();

				String strR = r.toString();

				// System.out.println(strR);

				// pairing + H1()
				String t = (params.pairing.pairing(
						params.h1Pre.pow(BigInteger.valueOf(lshValue)), params.g2))
						.powZn(repo.getKeyV()).toString();

				// System.out.println(t);

				// H2()
				long h = PRF.HMACSHA1ToUnsignedInt(t, strR);

				// System.out.println("c = " + c);

				SecureToken seT = new SecureToken(strR, h);
				
				secureTokens.add(seT);
			}
			
			SecureRecord sr = new SecureRecord(i + 1, secureTokens);
			
			repo.insert(sr);
			
			if ((i+1) % (lshVectors.size() / 100) == 0) {
                System.out.println("Inserting " + (i+1) / (lshVectors.size() / 100) + "%");
            }
		}
		
		/////
        
        long etOfInsert = System.currentTimeMillis();
        
        System.out.println("Insert time: " + (etOfInsert - startTimeOfInsert) + " ms.");

		// %%%%%%%%%%%%%%%%%% test %%%%%%%%%%%%%%%%%%%

		if (rawRecords == null || rawRecords.isEmpty()) {

			PrintTool.println(PrintTool.ERROR,
					"reading failed, please check the input file!");

			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean rootFlag = true;

		while (rootFlag) {
			System.out
					.print("\n\n----------------------- Root Menu -----------------------\n"
							+ "Please select an operation:\n"
							+ "[1]  throughput;\n"
							+ "[QUIT] quit system.\n\n"
							+ "--->");
			String inputStr;
			int operationType;
			try {
				inputStr = br.readLine();

				try {
					if (inputStr == null
							|| inputStr.toLowerCase().equals("quit")
							|| inputStr.toLowerCase().equals("q")) {

						System.out.println("Quit!");

						break;
					} else if (Integer.parseInt(inputStr) > 1
							|| Integer.parseInt(inputStr) < 1) {

						System.out.println("Warning: operation type should be limited in [1, 1], please try again!");

						continue;
					} else {
						operationType = Integer.parseInt(inputStr);
					}
				} catch (NumberFormatException e) {
					System.out.println("Warning: operation type should be limited in [1, 1], please try again!");

					continue;
				}

			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			if (operationType == SysConstant.OPERATION_THROUGHPUT) {
				System.out.println("\nModel: throughput test.");

				while (true) {
					System.out
							.println("\n\nNow, please set the number of users: (-1 means return to root menu)");

					String strNumOfUser = null;
					int userNum;
					String strNumOfRepo = null;
					int repoNum;
					
					String strStTime = null;
					long stTime;
					
					
					try {
						strNumOfUser = br.readLine();
						
						if (strNumOfUser.equals("-1")) {
							
							System.out.println("Return to root menu!");

							break;
						}

						System.out
						.println("\n\nNow, please set the number of repos: (-1 means return to root menu)");
						strNumOfRepo = br.readLine();
						
						if (strNumOfRepo.equals("-1")) {
							
							System.out.println("Return to root menu!");

							break;
						}
						
						System.out
						.println("\n\nNow, please set the startTime: (-1 means return to root menu)");
						strStTime = br.readLine();
						
						if (strStTime.equals("-1")) {
							
							System.out.println("Return to root menu!");

							break;
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						if (strNumOfUser == null || strNumOfUser.equals("-1") || strNumOfRepo == null || strNumOfRepo.equals("-1")|| strStTime == null || strStTime.equals("-1")) {

							System.out.println("Return to root menu!");

							break;
						} else if (Integer.parseInt(strNumOfUser) <=0 || Integer.parseInt(strNumOfRepo) <= 0) {

							System.out
									.println("Warning: query index should be larger than 0.");

							continue;
						} else {
							userNum = Integer.parseInt(strNumOfUser);
							
							repoNum = Integer.parseInt(strNumOfRepo);
							
							stTime = Long.parseLong(strStTime);

							System.out.println("Test on : "
									+ userNum + " users, " + repoNum + " repos.\n");
						}
					} catch (NumberFormatException e) {
						System.out
								.println("Warning: format error.");
						continue;
					}

					
					// prepare userNum of queries
					
					List<List<Element>> queries = new ArrayList<List<Element>>(userNum);
					List<MyCounter> throughput = new ArrayList<MyCounter>(userNum);
					
					//int[] users = new int[userNum];
					List<User> users = new ArrayList<User>(userNum);
					
					for (int i = 0; i < userNum; i++) {
						
						User user = new User(i+1, params, thirdParty.getKeyPublic());
						
						users.add(user);
						
						RawRecord queryRecord;
						
						queryRecord = queryRecords.get((i+1)%queryRecords.size());
						
						// prepare the query message
						List<Element> tArray = new ArrayList<Element>(lshL);
						

						long[] lshVector = lsh.computeLSH(queryRecord.getValue());
						
						for (int j = 0; j < lshL; j++) {
							
							Element t = params.h1Pre.pow(BigInteger.valueOf(lshVector[j])).powZn(user.getKeyV());
							
							tArray.add(t);
						}
						
						queries.add(tArray);
						
						// authorize the detector
						Element auth = contentProvider.authorize(user.getKeyVForAuthorization());
									
						// id = 0 is the detector
						repo.addDelta(user.getUid(), auth);
						
						throughput.add(new MyCounter());
					}
					
					// multithread to simulate
					if (userNum > 0) {
						
			        	//multiple threads
				        MyCountDown threadCounter3 = new MyCountDown(userNum);
				        
				        long nowTime = System.currentTimeMillis();
				        
				        for (int i = 0; i < userNum; i++) {
				        	
				        	ThroughputTestThread t = new ThroughputTestThread("Thread " + i, threadCounter3, users.get(i).getUid(), queries.get(i), repo, repoNum, throughput.get(i),nowTime + stTime*1000);

					        t.start();
					        
					        
				        }

				        // wait for all threads done
				        while (true) {
				            if (!threadCounter3.hasNext())
				                break;
				        }
					}
					
					Long total = 0L;
					
					for (int i = 0; i < userNum; i++) {
						total += throughput.get(i).getCtr();
					}
					
					System.out.println("Total throughput is : " + total);
				}
			}
		}
	}

	private static List<List<Long>> computeLSH(List<RawRecord> rawRecords,
			Parameters params) {
		
		List<List<Long>> lshVectors = new ArrayList<List<Long>>(rawRecords.size());

		for (int i = 0; i < rawRecords.size(); i++) {
			
			RawRecord rd = rawRecords.get(i);

			// compute LSH vector
			long[] lshVector = params.lsh.computeLSH(rd.getValue());
			
			List<Long> lshValues = new ArrayList<>(lshVector.length);
			
			for (int j = 0; j < lshVector.length; j++) {
				
				
				lshValues.add(lshVector[j]);
			}
			
			lshVectors.add(lshValues);
		}
		return lshVectors;
	}
	
	private static void encryptFP(List<RawRecord> rawRecords, Parameters params, RepositoryForThroughputTest repo) {

		//Map<Integer, EncryptedFingerprint> encryptedFingerprints = new HashMap<Integer, EncryptedFingerprint>();
		
		for (int i = 0; i < rawRecords.size(); i++) {
			
			// encrypt fingerprint
			//BigInteger cipherFP = Paillier.Enc(rawRecords.get(i).getValue(), repo.getKeyF());
			HashElGamalCiphertext cipherFP = HashElGamal.encrypt(params.hashElGamalPara, repo.getKeyF(), rawRecords.get(i).getValue());
			
			repo.getEncryptedFingerprints().put(rawRecords.get(i).getId(),
					new EncryptedFingerprint(rawRecords.get(i).getName(), cipherFP));
		}
	}
}
