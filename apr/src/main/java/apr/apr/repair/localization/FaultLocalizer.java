package apr.apr.repair.localization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;

import apr.apr.repair.utils.ClassFinder;
import apr.apr.repair.utils.FileUtil;

public class FaultLocalizer  {
	private String workDir = System.getProperty("user.dir");
	final static Logger logger = LoggerFactory.getLogger(FaultLocalizer.class);
	
	private int totalPassed = 0;
	private int totalFailed = 0;
	
	private List<String> failedTestsMethods = new ArrayList<>();
	
	private List<SuspiciousLocation> suspList = new ArrayList<>();
	
	private Set<String> testClasses = new HashSet<>();
	private Set<String> srcClasses = new HashSet<>();
	private String savePath;
	private String logPath;
	
	public FaultLocalizer(){
		
	}
	
	public FaultLocalizer(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses) {
//		this(null, null);
		this.savePath = savePath;
		this.logPath = logPath;
		this.testClasses.addAll(testClasses);
		this.srcClasses.addAll(srcClasses);
		localize(null);
	}
	
	public FaultLocalizer(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses, List<String> extraFailedTests) {
		this.savePath = savePath;
		this.logPath = logPath;
		this.testClasses.addAll(testClasses);
		this.srcClasses.addAll(srcClasses);
		localize(extraFailedTests);
	}
	
//	public FaultLocalizer(List<String> oriFailedTests, List<String> extraFailedTests) {
//		localize();
//	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 21, 2020
	 *
	 */
	public List<SuspiciousLocation> readFLResults(String flPath) {
		List<String> lines = FileUtil.readFile(flPath);
		List<SuspiciousLocation> suspList = new ArrayList<>();
		//com.google.javascript.rhino.Token:217,0.8164965809277261
		for (String line : lines){
			String className = line.split(":")[0];
			int lineNo = Integer.parseInt(line.split(":")[1].split(",")[0]);
			double suspValue = Double.parseDouble(line.split(":")[1].split(",")[1]);
			suspList.add(new SuspiciousLocation(className, lineNo, suspValue));
		}
		return suspList;
	}
	
	public Set<String> getExtraFailedTests(List<String> oriFailedTests){
		Set<String> extraFailedTests = new HashSet<>();
		
		Set<String> failedTests = new HashSet<>();
		for(String method : failedTestsMethods){
			String testName = method.split("#")[0];
			failedTests.add(testName);
			logger.info("fl failed test method: {}", testName);
		}
		logger.info("fl failed test methods size: {}, fl failed test size: {}", failedTestsMethods.size(), failedTests.size());
		
		for(String test : failedTests){
			if (! oriFailedTests.contains(test)){
				extraFailedTests.add(test);
				logger.info("extra failed test: {}", test);
			}
		}
		
		for(String test : oriFailedTests){
//			logger.info();
			if (! failedTests.contains(test)){
				logger.warn("original failed test ({}) does not fail!", test);
			}
		}
		
		return extraFailedTests;
	}
	
	public void localize(List<String> extraFailedTests){
		logger.info("FL starts.");
		
		GZoltar gz = null;
		try {
			gz = new GZoltar(workDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// set classpath
		gz.setClassPaths(FileUtil.depsList);
		
		gz.addTestPackageNotToExecute("junit.framework"); // prevent extra unrelated failed tests 
		gz.addTestPackageNotToExecute("org.junit");
		gz.addTestPackageNotToExecute("org.easymock");
		
		// seems does not work.
		gz.addTestPackageNotToExecute("junit.framework.TestSuite$1#warning");
		gz.addTestPackageNotToExecute("junit.framework.TestSuite$1");
		gz.addTestPackageNotToExecute("junit.framework.TestSuite");
		// test classes to execute
//		ClassFinder cf = new ClassFinder();
//		Set<String> testClasses = cf.getTestClasses(FileUtil.binTestDir, FileUtil.binJavaDir, FileUtil.depsList);
//		Set<String> testMethods = cf.getTestMethods();
		for(String testClass : testClasses){
			// filter extra failed tests
//			if (extraFailedTests != null){
//				for(String test : extraFailedTests){
//					gz.addTestNotToExecute(test);
//				}
//			}
			if (extraFailedTests != null && extraFailedTests.contains(testClass)){
				continue; 
			}
			
			if (testClass.contains("junit.framework")){
				continue;
			}
			
			gz.addTestToExecute(testClass);
		}
		
		gz.addPackageNotToInstrument("org.junit");
		gz.addPackageNotToInstrument("junit.framework");
		gz.addPackageNotToInstrument("org.easymock");
		// src classes to instrument
//		Set<String> srcClasses = cf.getJavaClasses(FileUtil.binJavaDir);
//		Set<String> srcClasses = cf.getJavaClasses(FileUtil.srcJavaDir, "java");
		for(String srcClass : srcClasses){
			gz.addClassToInstrument(srcClass);
		}
		
		gz.run();
		Spectra spectra = gz.getSpectra();
//		System.out.println(spectra.toString());
	
		// get test result
		List<TestResult> testResults = spectra.getTestResults();
		logger.info("Total tests executed: {}, total componenets (stmts) obtained: {}", testResults.size(), spectra.getNumberOfComponents());
		
		for (TestResult tr : testResults){
			if (tr.wasSuccessful()){
				totalPassed ++;
			}else{
				totalFailed ++;
				FileUtil.writeToFile(logPath, String.format("Failed test: %s, trace: \n%s \n\n", tr.getName(), tr.getTrace()));
				String fullTrace = tr.getTrace();
				
				
				// consider junit.framework.TestSuite$1
				if (tr.getName().startsWith("junit.framework.TestSuite$1")){
					String firstLine = fullTrace.split("\n")[0];
					if (firstLine.startsWith("junit.framework.AssertionFailedError: Class ")){
						String failedClass = firstLine.substring("junit.framework.AssertionFailedError: Class ".length()).split(" ")[0];
						failedTestsMethods.add(failedClass);
					}else{
						failedTestsMethods.add(tr.getName());
					}
				}else{
					failedTestsMethods.add(tr.getName());
				}
				
				if (fullTrace.length() > 150){
					fullTrace = fullTrace.substring(0, 150);
				}
				logger.info("Failed test: {}. \nTrace: \n{}", tr.getName(), fullTrace);
			}
		}
		
		FileUtil.writeToFile(logPath, String.format("Total passed tests: %d , total failed tests: %d\n", 
				totalPassed, totalFailed)); //apr. record
		
		// for each component (suspicious stmt)
		for(Component component : spectra.getComponents()){
			Statement stmt = (Statement) component;
			String className = stmt.getClazz().getLabel();
			int lineNo = stmt.getLineNumber();
			BitSet coverage = stmt.getCoverage();
//			Map<Integer, Integer> countMap = stmt.getCountMap();
			
//			logger.info("check coverage bitset size: {}", coverage.size());

			int execPassed = 0;
			int execFailed = 0;
			List<String> execPassedMethods = new ArrayList<>();
			List<String> execFailedMethods = new ArrayList<>();
			
			// traverse bitset coverage
			// this loop is based on the nextSetBit() javadoc
			for (int i = coverage.nextSetBit(0); i >= 0; i = coverage.nextSetBit(i+1)) {
				if (i == Integer.MAX_VALUE) {
					logger.error("i == Integer.MAX_VALUE now.");
					break; // or (i+1) would overflow
				}
				
				// operate on index i here
				TestResult tr = testResults.get(i);
				if (tr.wasSuccessful()){
					execPassed ++;
					execFailedMethods.add(tr.getName());
				}else{
					execFailed ++;
					execFailedMethods.add(tr.getName());
				}
			}
			
			SuspiciousLocation sl = new SuspiciousLocation(className, lineNo, execPassed, execFailed, totalPassed, totalFailed, execPassedMethods, execFailedMethods);
			suspList.add(sl);
		}
		
		Collections.sort(suspList, new Comparator<SuspiciousLocation>(){
			@Override
			public int compare(SuspiciousLocation o1, SuspiciousLocation o2) {
				// descending order
				return Double.compare(o2.getSuspValue(), o1.getSuspValue());
			}
		});
		
		for (SuspiciousLocation sl : suspList){
			FileUtil.writeToFile(savePath, sl.toString() + "\n");
		}
		
		changeFL(suspList);
		
		logger.info("FL ends.");
	}

	/** @Description  find buggy locs and move them into top positions
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 * @param suspList
	 */
	private void changeFL(List<SuspiciousLocation> suspList) {
		List <SuspiciousLocation> buggyLocs = FileUtil.readBuggylocFile(FileUtil.buggylocPath);
		List <Integer> buggyLocIndex = new ArrayList<>();
		
		List<SuspiciousLocation> suspListBackup = new ArrayList<>();
		suspListBackup.addAll(suspList);
		
		List<SuspiciousLocation> changedSuspList = new ArrayList<>();
		
		for (SuspiciousLocation sl : buggyLocs){
			int index = suspList.indexOf(sl);
			if (index >= 0){
				buggyLocIndex.add(index);
//				repairLocs.add(se);
				FileUtil.writeToFile(logPath, String.format("Buggy location: %s is localized, its rank index is: %d .\n", sl.toString(), index));
			}else{
				FileUtil.writeToFile(logPath, String.format("Buggy location: %s is not localized.\n", sl.toString()));
			}
		}
		
		Collections.sort(buggyLocIndex);
		
		// firstly add buggy locs
		for (int index : buggyLocIndex){
			changedSuspList.add(suspListBackup.get(index));
		}
		
		suspListBackup.removeAll(buggyLocs);
		changedSuspList.addAll(suspListBackup);
		
		String changedFlPath = savePath.replaceFirst(".txt", "_changed.txt");
		FileUtil.writeToFile(changedFlPath, "",false);
		for (SuspiciousLocation sl : changedSuspList){
			FileUtil.writeToFile(changedFlPath, sl.toString() + "\n");
		}
		logger.debug("bp here");
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
}
