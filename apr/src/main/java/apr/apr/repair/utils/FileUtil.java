package apr.apr.repair.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apr.apr.repair.localization.SuspiciousLocation;

public class FileUtil {
    final static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	// java/test classes
	public static List<String> srcClasses = new ArrayList<>();
	public static List<String> testClasses = new ArrayList<>();
	public static List<String> testMethods = new ArrayList<>();
	
	// parameter
	public static String srcJavaDir;
	public static String binJavaDir;
	public static String binTestDir;
	public static String dependencies;
	public static String buggylocDir;
	public static String changedFLPath;
	public static String externalProjPath;
	public static String jvmPath;
	public static String failedTestsStr;
	public static String gzoltarDir;
//	public static String bugDir;
//	public static String junitJar;
	
	public static ArrayList<String> depsList = new ArrayList<>();
	
//	public static List<String> oriFailedTestMethods;
	public static List<String> oriFailedTests;

	//save all positive tests
	public static String positiveTestsPath;
	public static String filteredPositiveTestsPath;
	public static List<String> fakedPosTests = new ArrayList<>();
	
	// buggy locs
	public static List<String> buggylocs = new ArrayList<>();
	public static List<String> relatedBuggylocs = new ArrayList<>();
	public static List<Integer> buggyLocIndex = new ArrayList<>();
	public static List<Integer> relatedBuggyLocIndex = new ArrayList<>();
	
	public static boolean getValidPatch = false;
	
	public static List<String> suspLocs = new ArrayList<>();
	
	
	
	public static int totalPassedTests = 0;
	public static int totalFailedTests = 0;
	
//	public static String flOutputPath;
//	public static String flLogPath;
//	public static String changedFLPath;
	
	public static String flPath;
	public static String flLogPath;
	public static String oriFLPath;
	public static String oriFlLogPath;
	public static String filteredFLPath;
	public static String filteredFlLogPath;
	public static String buggylocPath;
	public static String mpPath;
	public static String toolName = "MY_APR";
	// for simfix code search
	public static String searchLogPath;
	
//	public static List<Integer> buggyLocIndex = new ArrayList<>();
//	public static List<LCNode> buggyLocs = new ArrayList<>();
//	public static List<LCNode> allLocs = new ArrayList<>();
//	public static List<LCNode> positiveLocs = new ArrayList<>();
//	public static List<LCNode> localizedBuggyLocs = new ArrayList<>();
//	public static List<LCNode> mpLocs = new ArrayList<>();
//	public static List<LCNode> gzLocs = new ArrayList<>();
	
//	public static List<TestResult> testClasses = new ArrayList<fr.inria.lille.localization.TestResult>();
	// tbar not used
	public static long preBeforeFLStart; // before fl -> not used
	public static long flStart; //fl -> not used 
	public static long betweenFlPGStart; // between fl and pg. // PG: patch generation -> not used
	
	// tbar used
	public static long preBeforePGStart; // before pg -> used for tbar
	public static long pgAndVStart;// pg and pv // pgAndV: patch generation and validation
	
	// tbar not used
	public static long postPGStart;// after pg and pv preBeforeFLStart
	
	public static long stmtPGVStart;//total time cost on repairing a stmt including pg and pv
	public static long stmtPGStart; // patch generation time
	public static long stmtPVStart;// only covered tests
	public static long stmtCompileStart; //compile src code
	public static long stmtRunTestStart; //run covered/failed/partail tests
	public static long stmtRunAllTestsStart; //run all tests
	
	public static long allStart; // for total time cost of repairing a bug.
	public static long saveFlStart; // write fl results time start
	
	public static int patchCnt;
	
	public static void printTime(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("Current time: " + df.format(new Date()));
	}
	
	public static long getTime(){
		return System.currentTimeMillis();
	}
	
//	public static void setTime(){
//		this.allStart = System.currentTimeMillis();
//	}
	
	public static String countTime(long startTime){
		DecimalFormat dF = new DecimalFormat("0.0000");
//		Util.startTime = System.currentTimeMillis();
		return dF.format((float) (System.currentTimeMillis() - startTime)/1000);
	}
	
	public static void writeLinesToFile(String path, List<String> lines){
		writeLinesToFile(path, lines, false);
	}
	
	public static void writeLinesToFile(String path, Set<String> lines){
		writeLinesToFile(path, lines, false);
	}
	
	public static void writeLinesToFile(String path, Set<String> lines,  boolean append){
		List<String> linesList = new ArrayList<>();
		linesList.addAll(lines);
		writeLinesToFile(path, linesList, append);
	}
	
	public static void writeLinesToFile(String path, List<String> lines, boolean append){
		// get dir
		String dirPath = path.substring(0, path.lastIndexOf("/"));
		File dir = new File(dirPath);
		if (!dir.exists()){
			dir.mkdirs();
			System.out.println(String.format("%s does not exists, and are created now via mkdirs()", dirPath));
		}
		
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(path, append));
			for(String line : lines){
				output.write(line + "\n");
			}
//			output.write(content);
			output.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
	}
	
	// default: flLogPath
	public static void writeToFile(String content){
		writeToFile(FileUtil.flLogPath, content, true);
	}
	
	public static void writeToFile(String path, String content){
		writeToFile(path, content, true);
	}
	
	public static void writeToFile(String path, String content, boolean append){
		// get dir
		String dirPath = path.substring(0, path.lastIndexOf("/"));
		File dir = new File(dirPath);
		if (!dir.exists()){
			dir.mkdirs();
			System.out.println(String.format("%s does not exists, and are created now via mkdirs()", dirPath));
		}
		
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(path, append));
			output.write(content);
			output.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
	}
	
	public static List<String> readFile(String path){
		List<String> list = new ArrayList<>();
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
            	if (line.length() == 0) System.err.println(String.format("Empty line in %s", path));
            	list.add(line); // add line
            }
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return list;
	}
	
	public static List<SuspiciousLocation> readBuggylocFile(String path){
		List<SuspiciousLocation> buggyLocs = new ArrayList<>();
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
            	// e.g., com.google.javascript.jscomp.RemoveUnusedVars:380
            	SuspiciousLocation sl = new SuspiciousLocation(line.split(":")[0], Integer.parseInt(line.split(":")[1]));
            	buggyLocs.add(sl); // add test
            }
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return buggyLocs;
	}
	
	public static List<SuspiciousLocation> readStmtFile(String path){
		List<SuspiciousLocation> stmtList = new ArrayList<>();
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            in.readLine(); // skip first row
            while ((line = in.readLine()) != null) {
            	// e.g., org.jfree.data.general$AbstractDataset#AbstractDataset():95
            	if (line.length() == 0) logger.error("Empty line in %s", path);
            	
            	// bug fix: some cases have more than one $:
            	// spoon$Launcher$1#accept(java.io.File):676
            	String[] tmps = line.split(":")[0].split("#")[0].split("$");
            	String className = tmps[0] + "." + tmps[1];
            	SuspiciousLocation sl = new SuspiciousLocation(className, Integer.parseInt(line.split(":")[1])); //line.split(":")[0].split("#")[0].replace("$", ".")
            	stmtList.add(sl); // add test
            }
            logger.info(String.format("The total suspicious statements: %d", stmtList.size()));
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return stmtList;
	}
	
	public static List<String> readTestFile(String path){
		return readTestFile(path, true);
	}
	
	/**
	 * @Description e.g., /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Mockito_23/MY_APR/FL/unit_tests.txt
	 * or: /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Mockito_23/MY_APR/FL/sfl/txt/tests.csv
	 * @author apr
	 * @version Apr 10, 2020
	 *
	 * @param path
	 * @param isCsv
	 * @return
	 */
	public static List<String> readTestFile(String path, boolean isCsv){
		List<String> testsList = new ArrayList<>();
		int position = 1;
		if (isCsv){
			position = 0;
		}
		
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            in.readLine(); // skip first row
            while ((line = in.readLine()) != null) {
            	// (txt) e.g., JUNIT,org.jfree.chart.entity.junit.LegendItemEntityTests#testSerialization
            	// (csv) now become: org.jfree.chart.entity.junit.LegendItemEntityTests#testSerialization,PASS,118975132,
            	if (line.length() == 0) logger.error("Empty line in %s", path);
            	testsList.add(line.split(",")[position]); // add test
            }
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return testsList;
	}
	
	/**
	 * @Description e.g.,read all test methods from /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Closure_103/Dale_APR/FL/unit_tests.txt 
	 * @author apr
	 * @version Apr 4, 2020
	 *
	 * @param path
	 * @return
	 */
	public static List<String> readTestMethodFile(String path){
		List<String> testsList = new ArrayList<>();
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            in.readLine(); // skip first row
            while ((line = in.readLine()) != null) {
            	// e.g., JUNIT,org.jfree.chart.entity.junit.LegendItemEntityTests#testSerialization
            	if (line.length() == 0) logger.error("Empty line in %s", path);
            	testsList.add(line.split(",")[1]); // add test
            }
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return testsList;
	}
	
	public static List<Pair<List<Integer>, String>> readMatrixFile(String path, int specSize, List<String> testsList){
		List<Pair<List<Integer>, String>> matrixList = new ArrayList<>();
		try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            int cnt = 0;
            int unrelatedTestCnt = 0;
            
            while ((line = in.readLine()) != null) {
            	if (cnt == 0){
            		// check equality
            		if (line.replace(" ", "").length() != (specSize + 1)){
            			String str = String.format("line length(): {}, total stmts size + 1: {} + 1. They are not consistent. EXIT now.\n", line.replace(" ", "").length(), specSize);
            			FileUtil.writeToFile(str);
            			logger.error(str);
            			System.exit(0);
            		}
            	}
            	
            	if (line.length() == 0) logger.error(String.format("Empty line in %s", path));
            	String testResult = line.substring(line.length() - 1);
//            	if (! testResult.equals("+") && ! testResult.equals("-")){
//            		logger.error("Unknown testResult: %s", testResult);
//            	}
            	
//            	List<String coverageList = Arrays.asList(line.substring(0, line.length() - 1).trim().split(" "));
//            	String coverage = line.substring(0, line.length() - 1).replace(" ", "");
            	List<Integer> coveredStmtIndexList = new ArrayList<>();
            	String coverage = line.replace(" ", "");
            	int index = -1;
            	while( (index = coverage.indexOf("1", index + 1)) >= 0){ // add "+ 1", otherwise it's a infinite loop
            		coveredStmtIndexList.add(index);
            	}
            	if(coveredStmtIndexList.size() == 0){
//            		logger.info(String.format("The test case (index: %s) is not executed by any stmts in Spectra", cnt));
            		unrelatedTestCnt ++;
            	}
            	
            	if (testResult.equals("+")){
            		totalPassedTests += 1;
            	}else if(testResult.equals("-")){
            		totalFailedTests += 1;
            		FileUtil.writeToFile(String.format("[readMatrixFile] [Matrix Simplification] failed method (index: %d): %s\n", cnt, testsList.get(cnt)));
            		FileUtil.writeToFile(String.format("[readMatrixFile] [Matrix Simplification] Indexes of stmts covered by failed method (total number: %d): %s\n", coveredStmtIndexList.size(), coveredStmtIndexList.toString()));
            	}else{
            		logger.error(String.format("Unknown testResult: %s", testResult));
            	}
            	
            	cnt ++;
            	matrixList.add(new Pair<>(coveredStmtIndexList, testResult)); // add test
            }
            FileUtil.writeToFile(String.format("[readMatrixFile] [Matrix Simplification] the unrelated test cases: %d\n", unrelatedTestCnt));
            FileUtil.writeToFile(String.format("[readMatrixFile] [Matrix Simplification] the total test cases: %d\n", cnt));
            FileUtil.writeToFile(String.format("[readMatrixFile] [Matrix Simplification] the total stmts: %d\n", specSize));
            
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return matrixList;
	}
	
	public static List<SuspiciousLocation> parseMatrixFile(String path, List<SuspiciousLocation> slSpecList, List<String> testsList, List<String> failedMethods){
		// init matrix
		int specSize = slSpecList.size();
		int testSize = testsList.size();
	    int[][] matrix = new int[testSize][specSize + 1];
	    String[] lineSplit = null;
	    
	    int totalPassedCnt = 0;
	    int totalFailedCnt = 0;
	    
	    // refer to: https://www.novixys.com/blog/java-reading-large-file-efficiently/
		try(BufferedReader in = new BufferedReader(new FileReader(path))) {
		    // get first row
		    String line = in.readLine();
		    lineSplit = line.split(" ");
		    if (lineSplit[lineSplit.length - 1].equals("+")){
		    	lineSplit[lineSplit.length - 1] = "2"; // passed test
		    	totalPassedCnt ++;
		    }else{
		    	lineSplit[lineSplit.length - 1] = "-2";// failed test
		    	totalFailedCnt ++;
		    	
		    	failedMethods.add(testsList.get(0));
		    }
		    // check size 
		    if (lineSplit.length != (specSize + 1)){
    			String str = String.format("line length(): {}, total stmts size + 1: {} + 1. They are not consistent. EXIT now.\n", line.replace(" ", "").length(), specSize);
    			FileUtil.writeToFile(str);
    			logger.error("matrix-spectra size inconsistency error: {}", str);
    			System.exit(0);
    		}
		    
		    // deal with first row
            for(int i = 0; i < lineSplit.length; i++)
            {
                matrix[0][i] = Integer.valueOf(lineSplit[i]);
            }
		    
		    // deal with the rest rows
            // refer to: https://stackoverflow.com/questions/36368470/reading-a-text-file-horizontally-and-vertically-in-java
            int cnt = 1;
		    while ((line = in.readLine()) != null) {
		    	lineSplit = line.split(" ");
		    	if (lineSplit[lineSplit.length - 1].equals("+")){
			    	lineSplit[lineSplit.length - 1] = "2"; // passed test
			    	totalPassedCnt ++;
			    }else{
			    	lineSplit[lineSplit.length - 1] = "-2";// failed test
			    	totalFailedCnt ++;
			    	
			    	failedMethods.add(testsList.get(cnt));
			    }
                for(int i = 0; i < lineSplit.length; i++)
                {
                    matrix[cnt][i] = Integer.valueOf(lineSplit[i]);
                }
                cnt++;
		    }   
		} catch (final IOException e) {
            e.printStackTrace();
        }
		
		// check equality
		if (testSize != (totalPassedCnt + totalFailedCnt)){
			String str = String.format("testsList.size(): %d, totalPassedCnt: %d, totalFailedCnt: %d. They are not consistent. EXIT now.\n", testSize, totalPassedCnt, totalFailedCnt);
			FileUtil.writeToFile(str);
			logger.error("matrix-test inconsistency size error: {}", str);
			System.exit(0);
		}
		
		List<SuspiciousLocation> slList = new ArrayList<>();
		// bug fix for error "java.lang.IndexOutOfBoundsException: Index: 4182, Size: 4182" -> remove + 1
		for (int i = 0; i < specSize; i++){ // i_th column
			SuspiciousLocation sl = slSpecList.get(i);
			int executedPassedCount = 0;
			int executedFailedCount = 0;
			List<Integer> coveredTestIndexList = new ArrayList<>(); // save indexes of executed tests
			
			for (int j = 0; j < testSize; j++){ // j_th row. Test result
				if(matrix[j][i] == 1){  // executed by j_th row test case
					coveredTestIndexList.add(j);
					if(matrix[j][specSize] == 2){ // passed
						executedPassedCount ++;
					}else{
						executedFailedCount ++;
					}
				}
			}
			
			slList.add(new SuspiciousLocation(sl.getClassName(), 
					sl.getLineNo(), executedPassedCount, executedFailedCount,
					totalPassedCnt, totalFailedCnt, coveredTestIndexList));
		}
		
		return slList;
	}
}