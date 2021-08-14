package apr.module.fl.localization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;
import apr.module.fl.utils.FileUtil;
import apr.module.fl.utils.Pair;

public final class FaultLocalizerNopol extends GZoltar {
	private static String workDir = System.getProperty("user.dir");
	final static Logger logger = LogManager.getLogger(FaultLocalizerNopol.class);
	
	private int totalPassed = 0;
	private int totalFailed = 0;
	
	private List<String> failedMethods = new ArrayList<>();
	
	private List<SuspiciousLocation> suspList = new ArrayList<>();
	
	private Set<String> testClasses = new HashSet<>();
	private Set<String> srcClasses = new HashSet<>();
	private String savePath;
	private String logPath;
	
	public static FaultLocalizerNopol createInstance(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses) {
		try {
			return new FaultLocalizerNopol(savePath, logPath, testClasses, srcClasses);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static FaultLocalizerNopol createInstance(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses, HashSet<String> extraFailedMethods) {
		try {
			return new FaultLocalizerNopol(savePath, logPath, testClasses, srcClasses, extraFailedMethods);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private FaultLocalizerNopol(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses) throws FileNotFoundException, IOException {
//		this(null, null);
		super(workDir);
		
		this.savePath = savePath;
		this.logPath = logPath;
		this.testClasses.addAll(testClasses);
		this.srcClasses.addAll(srcClasses);
		localize(null);
	}
	
	private FaultLocalizerNopol(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses, HashSet<String> extraFailedMethods) throws FileNotFoundException, IOException {
		super(workDir);
		
		this.savePath = savePath;
		this.logPath = logPath;
		this.testClasses.addAll(testClasses);
		this.srcClasses.addAll(srcClasses);
		localize(extraFailedMethods);
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
	
//	public Set<String> getExtraFailedTests(List<String> oriFailedTests){
//		Set<String> extraFailedTests = new HashSet<>();
//		
//		Set<String> failedTests = new HashSet<>();
//		for(String method : failedMethods){
//			String testName = method.split("#")[0];
//			failedTests.add(testName);
//			logger.info("fl failed test method: {}", testName);
//		}
//		logger.info("fl failed test methods size: {}, fl failed test size: {}", failedMethods.size(), failedTests.size());
//		
//		for(String test : failedTests){
//			if (! oriFailedTests.contains(test)){
//				extraFailedTests.add(test);
//				logger.info("extra failed test: {}", test);
//			}
//		}
//		
//		for(String test : oriFailedTests){
////			logger.info();
//			if (! failedTests.contains(test)){
//				logger.warn("original failed test ({}) does not fail!", test);
//			}
//		}
//		
//		return extraFailedTests;
//	}
	
	/**
	 * @Description this fl is different from that of nopol.
	 * 1) this instruments the src classes found in the src classes dir, but nopol does not instruments junit test classes. This is a slight difference.
	 * 2) this considers extra failed test methods.
	 * 
	 * Also, I learned from that: no software can avoid bugs. So we don't have to persue 100% perfect sometimes, especially in the experiments, which may cost huge time.
	 * 
	 * @author apr
	 * @version Apr 12, 2020
	 *
	 * @param extraFailedMethods
	 */
	public void localize(HashSet<String> extraFailedMethods){
		logger.info("FL starts.");
		
		// set classpath
		this.setClassPaths(FileUtil.depsList);
		
		this.addPackageNotToInstrument("org.junit");
		this.addPackageNotToInstrument("junit.framework");
		this.addPackageNotToInstrument("org.easymock");
		this.addTestPackageNotToExecute("junit.framework");
		this.addTestPackageNotToExecute("org.junit");
		this.addTestPackageNotToExecute("org.easymock");
		
		// seems does not work.
		this.addTestPackageNotToExecute("junit.framework.TestSuite$1#warning");
		this.addTestPackageNotToExecute("junit.framework.TestSuite$1");
		this.addTestPackageNotToExecute("junit.framework.TestSuite");

		// test classes to execute
		for(String testClass : testClasses){			
			if (testClass.contains("junit.framework")){
				continue;
			}
			
			this.addTestToExecute(testClass);
			this.addClassNotToInstrument(testClass);
		}
		
		if (extraFailedMethods != null){
			for(String extraFailedMethod : extraFailedMethods){
				this.addTestNotToExecute(extraFailedMethod);
			}
		}
		
		for(String srcClass : srcClasses){
			this.addClassToInstrument(srcClass);
		}
		
		logger.debug("FL starts gz.run()");
		final String systemClasspath = System.getProperty("java.class.path");

		// remove classpath noise
		String[] deps = systemClasspath.split(":");
		StringBuilder cl = new StringBuilder();
		for (int i = 0; i < deps.length; i++) {
			String dep = deps[i];
			if (dep.contains("jre") || dep.contains("gzoltar") || dep.contains("nopol")) {
				cl.append(dep).append(":");
			}
		}
		try {
			System.setProperty("java.class.path", cl.toString());
			setGzoltarDebug(false);
			this.run();
		} finally {
			System.setProperty("java.class.path", systemClasspath);
		}
		logger.debug("FL ends gz.run()");
		Spectra spectra = this.getSpectra();
//		System.out.println(spectra.toString());
	
		// get test result
		List<TestResult> testResults = spectra.getTestResults();
		logger.info("Total tests executed: {}, total componenets (stmts) obtained: {}", testResults.size(), spectra.getNumberOfComponents());
		FileUtil.writeToFile(logPath, String.format("[localize] total tests executed: %s, total componenets (stmts) obtained: %s\n", testResults.size(), spectra.getNumberOfComponents()));
				
		for (TestResult tr : testResults){
			if (tr.wasSuccessful()){
				totalPassed ++;
			}else{
				totalFailed ++;
				FileUtil.writeToFile(logPath, String.format("[localize] failed test: %s, trace: \n%s\n\n", tr.getName(), tr.getTrace()));
				String fullTrace = tr.getTrace();
				
				
				// consider junit.framework.TestSuite$1
				if (tr.getName().startsWith("junit.framework.TestSuite$1")){
					String firstLine = fullTrace.split("\n")[0];
					if (firstLine.startsWith("junit.framework.AssertionFailedError: Class ")){
						String failedClass = firstLine.substring("junit.framework.AssertionFailedError: Class ".length()).split(" ")[0];
						failedMethods.add(failedClass);
					}else{
						failedMethods.add(tr.getName());
					}
				}else{
					failedMethods.add(tr.getName());
				}
				
//				if (fullTrace.length() > 150){
//					fullTrace = fullTrace.substring(0, 150);
//				}
//				logger.info("Failed test: {}. \nTrace: \n{}", tr.getName(), fullTrace);
//				FileUtil.writeToFile(logPath, String.format("[localize] Failed test: %s\nTrace:\n%s\n", tr.getName(), fullTrace));
			}
		}
		
		FileUtil.writeToFile(logPath, String.format("[localize] Total passed tests: %d , total failed tests: %d\n", totalPassed, totalFailed)); //apr. record
		logger.info(String.format("[localize] Total passed tests: %d , total failed tests: %d\n", totalPassed, totalFailed));
		
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
		
		int posCnt = 0;
		for (SuspiciousLocation sl : suspList){
			FileUtil.writeToFile(savePath, sl.toString() + "\n");
			
			if(sl.getSuspValue() > 0){
				posCnt ++;
			}
		}
		FileUtil.writeToFile(logPath, String.format("[localize] total suspList size: %d, total positive suspList size: %d\n", suspList.size(), posCnt)); //apr. record
		
		changeFL(suspList);
		
		logger.info("FL ends.");
	}
	
	protected void setGzoltarDebug(boolean debugValue) {
		try {
			Field debug = com.gzoltar.core.agent.Launcher.class.getDeclaredField("debug");
			debug.setAccessible(true);
			debug.setBoolean(null, debugValue);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
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
				
				double bugSuspValue = suspList.get(index).getSuspValue();
				Pair <Integer, Integer> range =  FileUtil.getTieRange(index, bugSuspValue, suspList);
				
//				repairLocs.add(se);
				FileUtil.writeToFile(logPath, 
						String.format(
						"[changeFL] buggy location: %s is localized, its rank index is: %d, suspiciousness: %s, tie size:[%d, %d]\n", 
						sl.toString(), index, bugSuspValue, range.getLeft(), range.getRight()));
			}else{
				FileUtil.writeToFile(logPath, String.format("[changeFL] buggy location: %s is not localized.\n", sl.toString()));
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
//		logger.debug("bp here");
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public List<String> getFailedMethods() {
		return failedMethods;
	}

	public void setFailedMethods(List<String> failedMethods) {
		this.failedMethods = failedMethods;
	}
}
