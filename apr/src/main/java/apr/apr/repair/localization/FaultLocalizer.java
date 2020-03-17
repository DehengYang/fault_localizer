package apr.apr.repair.localization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;

import apr.apr.repair.utils.ClassFinder;
import apr.apr.repair.utils.FileUtil;

public class FaultLocalizer  {
	private static String workDir = System.getProperty("user.dir");
	final static Logger logger = Logger.getLogger(FaultLocalizer.class);
	
	public FaultLocalizer() {
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
		
		// src classes to instrument
//		Set<String> srcClasses = cf.getJavaClasses(FileUtil.binJavaDir);
		Set<String> srcClasses = cf.getJavaClasses(FileUtil.srcJavaDir, "java");
		for(String srcClass : srcClasses){
			gz.addClassToInstrument(srcClass);
		}
		
		gz.run();
		Spectra spectra = gz.getSpectra();
//		System.out.println(spectra.toString());
	
		// get test result
		List<TestResult> testResults = spectra.getTestResults();
		
//		for ()
	}
	
}
