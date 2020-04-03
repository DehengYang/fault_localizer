/**
 * 
 */
package apr.apr.repair.localization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apr.apr.repair.utils.CmdUtil;
import apr.apr.repair.utils.FileUtil;
import apr.apr.repair.utils.Pair;


/**
 * support GZoltar 1.7.3
 * @author apr
 * @version Apr 1, 2020
 *
 */
public class FaultLocalizer2 {
	final static Logger logger = LoggerFactory.getLogger(FaultLocalizer2.class);
	
	// parameters we need
	private String data_dir; //to save fl results
	private String bug_dir;
	private String test_classpath;
	private String test_classes_dir;
	private String src_classes_dir;
	private String src_classes_file;
	private String all_tests_file;
	private String junit_jar;
	
	// test:
	// 1) other benchmark bugs
	
	public FaultLocalizer2(){
		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/FL"; // direcory
		bug_dir = FileUtil.bugDir;
		test_classpath = FileUtil.dependencies;
		test_classes_dir = FileUtil.binTestDir;
		src_classes_dir = FileUtil.binJavaDir;
		src_classes_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/srcClasses.txt";
		all_tests_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/testClasses.txt";
		junit_jar = FileUtil.junitJar;
	}
	
	/**
	 * @Description get fl results 
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void localize(){
		String cmd = FileUtil.gzoltarDir + "/runGZoltar.sh" + " " + data_dir + " " + bug_dir + " " + test_classpath + " " + test_classes_dir + " "
				+ src_classes_dir + " " + src_classes_file + " " + all_tests_file + " " + junit_jar;
		CmdUtil.runCmdNoOutput(cmd);	
	}
	
	/**
	 * @Description get changed fl.
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void logFL(boolean simplify){
		String flResultDir = data_dir + "/sfl/txt";
		
		List<SuspiciousLocation> slSpecList;
		List<Pair<List<Integer>, String>> matrixList;
		
		// simplify
		if (simplify){
			String cmdSimplify = String.format("cp %s/matrix_simplify.py %s && cd %s && python3.6 matrix_simplify.py", FileUtil.gzoltarDir, data_dir, data_dir);
			String output = CmdUtil.runCmd(cmdSimplify);
			slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.faulty.csv");
			matrixList = FileUtil.readMatrixFile(flResultDir + "/filtered_matrix.txt", slSpecList.size());
		}else{
			// read spectra
			slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.csv");
			matrixList = FileUtil.readMatrixFile(flResultDir + "/matrix.txt", slSpecList.size());
		}
		
		List<String> testsList = FileUtil.readTestFile(data_dir + "/unit_tests.txt");
		
		int totalPassedTests = FileUtil.totalPassedTests;
		int totalFailedTests = FileUtil.totalFailedTests;
		
		// check equality
		if (testsList.size() != (totalPassedTests + totalFailedTests)){
			String str = String.format("testsList.size(): %d, totalPassedTests: %d, totalFailedTests: %d. They are not consistent. EXIT now.\n", testsList.size(), totalPassedTests, totalFailedTests);
			FileUtil.writeToFile(str);
			logger.error(str);
			System.exit(0);
		}
		
		// check equality
		
		List<SuspiciousLocation> slList = new ArrayList<>();
		//for (SourceLocation sl : slList){
		for (int i = 0; i < slSpecList.size(); i++){
			SuspiciousLocation sl = slSpecList.get(i);
			int executedPassedCount = 0;
			int executedFailedCount = 0;
			for (Pair<List<Integer>, String> pair : matrixList){
				if (pair.getLeft().contains(i)){
					if (pair.getRight().equals("+")){
						executedPassedCount += 1;
					}else{
						executedFailedCount += 1;
					}
				}
			}
			
			slList.add(new SuspiciousLocation(sl.getClassName(), 
					sl.getLineNo(), executedPassedCount, executedFailedCount,
					totalPassedTests, totalFailedTests));
			
		}
		Collections.sort(slList, new Comparator<SuspiciousLocation>(){
			@Override
			public int compare(final SuspiciousLocation o1, final SuspiciousLocation o2){
				return Double.compare(o2.getSuspValue(), o1.getSuspValue());
			}
		});
		
		// write to file.
		String writePath = flResultDir + "/ochiai-calculate.csv";
		FileUtil.writeToFile(writePath, "", false);
		for (SuspiciousLocation sl : slList){
			String line = sl.getClassName() + ":" + sl.getLineNo() + ";" + sl.getSuspValue() + "\n";
			FileUtil.writeToFile(writePath, line);
		}
		
//		for (int i = 0; i < matrixList.size(); i++){
//			boolean testResult;
//			if(matrixList.get(i).getRight().equals("+")){
//				testResult = true;
//			}else{
//				testResult = false;
//			}
//			List<Integer> coveredStmtIndexList = matrixList.get(i).getLeft();
//			
//			TestResultImpl test = new TestResultImpl(TestCase.from(this.testsList.get(i)), testResult);
//			
//			for(int index : coveredStmtIndexList){
//				SourceLocation sl = slList.get(index);
//				
//				if (!results.containsKey(sl)) {
//					results.put(sl, new ArrayList<fr.inria.lille.localization.TestResult>());
//				}
//				results.get(sl).add(test);
//			}
//		}
//		
//		LinkedHashMap<SourceLocation, List<fr.inria.lille.localization.TestResult>> map = new LinkedHashMap<>();
//		for (StatementSourceLocation ssl : sslList){
//			map.put(ssl.getLocation(), results.get(ssl.getLocation()));
//		}
	}
	

	public String getData_dir() {
		return data_dir;
	}

	public void setData_dir(String data_dir) {
		this.data_dir = data_dir;
	}


	public String getBug_dir() {
		return bug_dir;
	}


	public void setBug_dir(String bug_dir) {
		this.bug_dir = bug_dir;
	}


	public String getTest_classpath() {
		return test_classpath;
	}


	public void setTest_classpath(String test_classpath) {
		this.test_classpath = test_classpath;
	}


	public String getTest_classes_dir() {
		return test_classes_dir;
	}


	public void setTest_classes_dir(String test_classes_dir) {
		this.test_classes_dir = test_classes_dir;
	}


	public String getSrc_classes_dir() {
		return src_classes_dir;
	}


	public void setSrc_classes_dir(String src_classes_dir) {
		this.src_classes_dir = src_classes_dir;
	}


	public String getSrc_classes_file() {
		return src_classes_file;
	}


	public void setSrc_classes_file(String src_classes_file) {
		this.src_classes_file = src_classes_file;
	}


	public String getAll_tests_file() {
		return all_tests_file;
	}


	public void setAll_tests_file(String all_tests_file) {
		this.all_tests_file = all_tests_file;
	}


	public String getJunit_jar() {
		return junit_jar;
	}


	public void setJunit_jar(String junit_jar) {
		this.junit_jar = junit_jar;
	}
	
}
