package apr.apr.repair.localization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
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
	
	private List<SuspiciousLocation> suspList = new ArrayList<>();
	
	public FaultLocalizer() {
		localize();
	}
	
	public FaultLocalizer(List<String> filteredTests) {
		localize();
	}
	
	public void localize(){
		logger.info("FL starts.");
		
		GZoltar gz = null;
		try {
			gz = new GZoltar(workDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set classpath
		gz.setClassPaths(FileUtil.depsList);
		
		// test classes to execute
		ClassFinder cf = new ClassFinder();
		Set<String> testClasses = cf.getTestClasses(FileUtil.binTestDir, FileUtil.binJavaDir, FileUtil.depsList);
//		Set<String> testMethods = cf.getTestMethods();
		for(String testClass : testClasses){
			gz.addTestToExecute(testClass);
		}
		gz.addTestPackageNotToExecute("junit.framework"); // prevent extra unrelated failed tests 
		gz.addTestPackageNotToExecute("org.junit");
		gz.addTestPackageNotToExecute("org.easymock");
		
		// src classes to instrument
//		Set<String> srcClasses = cf.getJavaClasses(FileUtil.binJavaDir);
		Set<String> srcClasses = cf.getJavaClasses(FileUtil.srcJavaDir, "java");
		for(String srcClass : srcClasses){
			gz.addClassToInstrument(srcClass);
		}
		gz.addPackageNotToInstrument("org.junit");
		gz.addPackageNotToInstrument("junit.framework");
		gz.addPackageNotToInstrument("org.easymock");
		
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
				logger.info("Failed test: {}. \nTrace: {}", tr.getName(), tr.getTrace());
			}
		}
		
		// for each component (suspicious stmt)
		for(Component component : spectra.getComponents()){
			Statement stmt = (Statement) component;
			String className = stmt.getClazz().getLabel();
			int lineNo = stmt.getLineNumber();
			BitSet coverage = stmt.getCoverage();
//			Map<Integer, Integer> countMap = stmt.getCountMap();
			
			logger.info("check coverage bitset size: {}", coverage.size());

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
			FileUtil.writeToFile(FileUtil.oriFLPath, sl.toString() + "\n");
		}
		
		logger.info("FL ends.");
	}
}
