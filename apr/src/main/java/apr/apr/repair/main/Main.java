package apr.apr.repair.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;

import apr.apr.repair.execute.PatchTest;
import apr.apr.repair.localization.FaultLocalizer;
import apr.apr.repair.localization.FaultLocalizer2;
import apr.apr.repair.localization.SuspiciousLocation;
import apr.apr.repair.parser.AttemptFileParser;
import apr.apr.repair.parser.ClassNode;
import apr.apr.repair.parser.ClassVarParser;
import apr.apr.repair.parser.CodeBlocks;
import apr.apr.repair.parser.CodeFragment;
import apr.apr.repair.parser.NodeFinder;
import apr.apr.repair.utils.ClassFinder;
import apr.apr.repair.utils.FileUtil;
import apr.apr.repair.utils.NodeUtil;


public class Main {
	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args){
		long mainStartTime = System.currentTimeMillis();
		
		// get parameters
		setParameters(args);
		
		// get src & test clasess, and then save them to file
		long startTime = System.currentTimeMillis();
		ClassFinder cf = new ClassFinder();
		Set<String> testClasses = cf.getTestClasses(FileUtil.binTestDir, FileUtil.binJavaDir, FileUtil.depsList);
		Set<String> srcClasses = cf.getJavaClasses(FileUtil.srcJavaDir, "java");
		String testClassesPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/testClasses.txt";
		String srcClassesPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/srcClasses.txt";
		FileUtil.writeLinesToFile(srcClassesPath, srcClasses);
		FileUtil.writeLinesToFile(testClassesPath, testClasses);
		FileUtil.writeToFile(String.format("[Main] [time cost] of src and test classes collection: %s\n", FileUtil.countTime(startTime)));
		
		// fault localization v0.1.1
		faultLocalize(testClasses, srcClasses);
		
		// fl v1.7.3
		faultLocalize2(testClasses, srcClasses);
		// old version, not used now.
//		long startTime = System.currentTimeMillis();
//		FaultLocalizer2 fl = new FaultLocalizer2();
////		fl.localize();
////		fl.logFL(true); // simplify
////		fl.logFL(); // no simplification
//		FileUtil.writeToFile(String.format("fl (v1.7.3) time cost: %s\n", FileUtil.countTime(startTime)));
////		System.exit(0);
		
		// replicate all tests
//		replicateTests(testClasses); // old version, not used now
		startTime = System.currentTimeMillis();
		replicateTests(testClassesPath);
		FileUtil.writeToFile(String.format("[Main] [time cost] of replicateTests: %s\n", FileUtil.countTime(startTime)));
		
		FileUtil.writeToFile(String.format("[Main] [time cost] of whole main(): %s\n", FileUtil.countTime(mainStartTime)));
		System.exit(0);
		
		
		// read fl results from file
//		FaultLocalizer fl = new FaultLocalizer();
//		List<SuspiciousLocation> suspList = fl.readFLResults(FileUtil.flPath);
		
		// parse java files into ast
//		for(String srcClass : srcClasses){
//			AttemptFileParser fp = new AttemptFileParser(srcClass, FileUtil.srcJavaDir);
////			codeFinder.parse(srcClass, FileUtil.srcJavaDir, 88); //70, 215, 214, 64, 59, 60, 312, 382, 87
//			// /mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/com/google/javascript/jscomp/CoalesceVariableNames.java
//			break;
//		}
		
		// get all vars defined in Classes
		long startT = FileUtil.getTime();
		ClassVarParser cvp =  new ClassVarParser(new ArrayList<>(srcClasses), FileUtil.srcJavaDir);
		Map<String, ClassNode> classVarMap = cvp.getClassVarMap();
		logger.debug("classVarMap collection time cost: {}", FileUtil.countTime(startT));
//		cvp.printClassVarMap();
		
		// get all code blocks DataPackageResources_pl & ThermometerPlot
		for (Map.Entry<String, ClassNode> entry : classVarMap.entrySet()){
			String className = entry.getKey();
			logger.info("current class: {}", className);
			startT = FileUtil.getTime();
			ClassNode cn = entry.getValue();
			
//			cn = classVarMap.get("DialCap");
			
			CodeBlocks cbs = new CodeBlocks(cn.getCu());
			
			cn.setCbs(cbs);
			
			logger.debug("class for code block: {}, time cost: {}", className, FileUtil.countTime(startT));
		}
//		for (Map.Entry<String, ClassNode> entry : classVarMap.entrySet()){
//			String className = entry.getKey();
//			ClassNode cn = entry.getValue();
//			logger.debug("a breakpoint");
//		}
		
		// get/list all variables for the given file
//		NodeFinder sf = new NodeFinder(78, "com.google.javascript.jscomp.CoalesceVariableNames", 
//				"/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/", "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"); 
//		sf.getAllVariables(classVarMap);
//		sf.printVars();
		
//		repairLocations(suspList, classVarMap);
	}

	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param suspList
	 */
	private static void repairLocations(List<SuspiciousLocation> suspList, Map<String, ClassNode> classVarMap) {
		for (SuspiciousLocation sl : suspList){
			CodeFragment cf = new CodeFragment(sl.getLineNo(), sl.getClassName(), FileUtil.srcJavaDir);
			
			for (Map.Entry<String, ClassNode> entry : classVarMap.entrySet()){
				String className = entry.getKey();
				logger.info("current class: {}", className);
//				startT = FileUtil.getTime();
				ClassNode cn = entry.getValue();
				
				NodeUtil.getSimilarCode(cf, cn.getCbs());
			}
		}
		
	}

	/**
	 * @Description for gzoltar 1.7.3, conduct a second run if there exist extra failed methods 
	 * @author apr
	 * @version Apr 10, 2020
	 *
	 * @param testClasses
	 * @param srcClasses
	 */
	private static void faultLocalize2(Set<String> testClasses, Set<String> srcClasses) {
		// first fl run
		long startTime = System.currentTimeMillis();
		FaultLocalizer2 fl = new FaultLocalizer2();
		fl.localize();
//		fl.logFL(true); // simplify [not used now]
		fl.logFL(); // no simplification
		fl.getCoveredStmtsInfo();
		List<String> failedMethods = fl.getFailedMethods();
		FileUtil.writeToFile(String.format("[faultLocalize2] [time cost] of first fl (v1.7.3): %s\n", FileUtil.countTime(startTime)));
		
		// get extra failed cases (copy from replicate, and then modify)
		List<String> extraFailedMethods = new ArrayList<>();
		for (String failedMethod : failedMethods){
			if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
				extraFailedMethods.add(failedMethod);
				FileUtil.writeToFile(String.format("[faultLocalize2] First fl (v1.7.3) extra failed test method: %s\n", failedMethod));
			}
		}
		FileUtil.writeToFile(String.format("[faultLocalize2] First fl (v1.7.3) extra failed test method count: %s\n", extraFailedMethods.size()));
		// check if there is any expected failed test.
		if (extraFailedMethods.size() == failedMethods.size()){
			FileUtil.writeToFile("[faultLocalize2] First fl (v1.7.3) no expected failed tests are found. Exit now.\n");
			System.exit(0);
		}
		
		if (extraFailedMethods.isEmpty()){
			return; 
		}
		
		// else, start second fl run
		// get new unit_test file for second run
		String oriUnitTestsFile = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/unit_tests.txt";
		String newUnitTestsFile = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/new_unit_tests.txt"; 
		List<String> oriUnitTests = FileUtil.readTestFile(oriUnitTestsFile, false); // is not csv file -> false
		int oriUnitTestsSize = oriUnitTests.size();
		oriUnitTests.removeAll(extraFailedMethods);
		
		// extra check if extraFailedMethods are contained in oriUnitTests
		if(oriUnitTestsSize == oriUnitTests.size()){
			FileUtil.writeToFile("[faultLocalize2] Second fl (v1.7.3) extraFailedMethods are not found in oriUnitTests. Exit now.\n");
			System.exit(0);
		}
		
		// write new unit_test file
		FileUtil.writeToFile(newUnitTestsFile, "", false); //init
		for (String unitTest : oriUnitTests){
			FileUtil.writeToFile(newUnitTestsFile, "JUNIT," + unitTest + "\n");
		}
		
		// new fl
		startTime = System.currentTimeMillis();
		FaultLocalizer2 secondFl = new FaultLocalizer2(newUnitTestsFile);
		secondFl.localize();
		secondFl.logFL(); // no simplification
		secondFl.getCoveredStmtsInfo();
		List<String> secondFailedMethods = fl.getFailedMethods();
		List<String> secExtraFailedMethods = new ArrayList<>();
		FileUtil.writeToFile(String.format("[faultLocalize2] [time cost] of second fl (v1.7.3): %s\n", FileUtil.countTime(startTime)));
		
		for (String failedMethod : secondFailedMethods){
			if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
				secExtraFailedMethods.add(failedMethod);
				FileUtil.writeToFile(String.format("[faultLocalize2] Second fl (v1.7.3) extra failed test method: %s\n", failedMethod));
			}
		}
		FileUtil.writeToFile(String.format("[faultLocalize2] second fl (v1.7.3) extra failed test method count: %s\n", secExtraFailedMethods.size()));
		
		if (secExtraFailedMethods.size() == secondFailedMethods.size()){
			FileUtil.writeToFile("[faultLocalize2] second fl (v1.7.3) no expected failed tests are found. Exit now.\n");
			System.exit(0);
		}
	}
	
	/** @Description fault localization & re-fl if extra failed tests found
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 */
	private static void faultLocalize(Set<String> testClasses, Set<String> srcClasses) {
		long startTime = System.currentTimeMillis();
		
		FaultLocalizer fl = new FaultLocalizer(FileUtil.oriFLPath, FileUtil.oriFlLogPath, testClasses, srcClasses);
		List<String> failedMethods = fl.getFailedMethods();
		
		List<String> extraFailedMethods = new ArrayList<>();
		for (String failedMethod : failedMethods){
			if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
				extraFailedMethods.add(failedMethod);
				FileUtil.writeToFile(String.format("[faultLocalize] First fl (v0.1.1) extra failed test method: %s\n", failedMethod));
			}
		}
		FileUtil.writeToFile(String.format("[faultLocalize] First fl (v0.1.1) extra failed test method count: %s\n", extraFailedMethods.size()));
		// check if there is any expected failed test.
		if (extraFailedMethods.size() == failedMethods.size()){
			FileUtil.writeToFile("[faultLocalize] First fl (v0.1.1) no expected failed tests are found. Exit now.\n");
			System.exit(0);
		}
		
		FileUtil.writeToFile(String.format("[faultLocalize] [time cost] of first fl (v0.1.1): %s\n", FileUtil.countTime(startTime)));
		
		if (! extraFailedMethods.isEmpty()){
			startTime = System.currentTimeMillis();
			
//			logger.info("re-run fl due to {} extra failed test(s) in current FL.", extraFailedTests.size());
			FaultLocalizer flSecond = new FaultLocalizer(FileUtil.filteredFLPath, FileUtil.filteredFlLogPath, testClasses, srcClasses, new HashSet<>(extraFailedMethods));			
			List<String> secFailedMethods = flSecond.getFailedMethods();
			List<String> secExtraFailedMethods = new ArrayList<>();
			for (String failedMethod : secFailedMethods){
				if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
					secExtraFailedMethods.add(failedMethod);
					FileUtil.writeToFile(String.format("[faultLocalize] Second fl (v0.1.1) extra failed test method: %s\n", failedMethod));
				}
			}
			FileUtil.writeToFile(String.format("[faultLocalize] Second fl (v0.1.1) extra failed test method count: %s\n", secExtraFailedMethods.size()));
			// check if there is any expected failed test.
			if (secExtraFailedMethods.size() == secFailedMethods.size()){
				FileUtil.writeToFile("[faultLocalize] Second fl (v0.1.1) no expected failed tests are found. Exit now.\n");
				System.exit(0);
			}
			
			FileUtil.writeToFile(String.format("[faultLocalize] [time cost] of second fl (v0.1.1): %s\n", FileUtil.countTime(startTime)));
		}
	}

	/*
	 * receive parameters
	 */
	private static void setParameters(String[] args) {		
//		Map<String, String> parameters = new HashMap<>();
		
        Option opt1 = new Option("srcJavaDir","srcJavaDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/src");
        opt1.setRequired(true);
        Option opt2 = new Option("binJavaDir","binJavaDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/build/classes/main/ ");
        opt2.setRequired(true);   
        Option opt3 = new Option("binTestDir","binTestDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/build/classes/test/ ");
        opt3.setRequired(true);
        Option opt4 = new Option("dependences","dependences",true,"all dependencies");
        opt4.setRequired(true);
        Option opt5 = new Option("buggylocDir","buggylocDir",true,"e.g., /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Mockito_10/");
        opt5.setRequired(true);
        Option opt6 = new Option("externalProjPath","externalProjPath",true,"Path to run junit tests. e.g., /home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        opt6.setRequired(true);
        Option opt7 = new Option("jvmPath","jvmPath",true,"java path to run junit tests (e.g., /home/apr/env/jdk1.7.0_80/jre/bin/java)");
        opt7.setRequired(true);
        Option opt8 = new Option("failedTestsStr","failedTestsStr",true,"e.g., com.google.javascript.jscomp.CollapseVariableDeclarationsTest");
        opt8.setRequired(true);
        Option opt9 = new Option("gzoltarDir","gzoltarDir",true,"e.g., /mnt/recursive-repairthemall/RepairThemAll-Nopol/libs/gzoltar1.7.3/");
        opt9.setRequired(true);
//        Option opt10 = new Option("bugDir","bugDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/");
//        opt10.setRequired(true);
//        Option opt11 = new Option("junitJar","junitJar",true,"e.g., /mnt/recursive-repairthemall/RepairThemAll-Nopol/script/../benchmarks/defects4j/framework/projects/lib/junit-4.11.jar");
//        opt11.setRequired(true);

        Options options = new Options();
        options.addOption(opt1);
        options.addOption(opt2);
        options.addOption(opt3);
        options.addOption(opt4);
        options.addOption(opt5);
        options.addOption(opt6);
        options.addOption(opt7);
        options.addOption(opt8);
        options.addOption(opt9);
//        options.addOption(opt10);
//        options.addOption(opt11);

        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        try {
            cli = cliParser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            helpFormatter.printHelp(">>>>>> test cli options", options);
            e.printStackTrace();
        } 
        
        try {
	        if (cli.hasOption("srcJavaDir")){
	        	String srcJavaDir = cli.getOptionValue("srcJavaDir");
	        	
	        	FileUtil.srcJavaDir = new File(srcJavaDir).getCanonicalPath();
	//        	parameters.put("srcJavaDir", cli.getOptionValue("srcJavaDir"));
	        }
	        if(cli.hasOption("binJavaDir")){
	        	String binJavaDir = cli.getOptionValue("binJavaDir");
	        	
	        	FileUtil.binJavaDir = new File(binJavaDir).getCanonicalPath();
	//        	parameters.put("binJavaDir", cli.getOptionValue("binJavaDir"));
	        }
	        if(cli.hasOption("binTestDir")){
	        	String binTestDir = cli.getOptionValue("binTestDir");
	        	
	        	FileUtil.binTestDir = new File(binTestDir).getCanonicalPath();
	        	
	//        	parameters.put("binTestDir", cli.getOptionValue("binTestDir"));
	        }
	        if(cli.hasOption("dependences")){
	        	FileUtil.dependencies = cli.getOptionValue("dependences");
	        	for(String dep : FileUtil.dependencies.split(":")){
	        		if(! dep.isEmpty() && new File(dep).exists()){
	        			
	        				String path = new File(dep).getCanonicalPath();
	        				if (FileUtil.depsList.contains(path)) continue;
	        				
							FileUtil.depsList.add(path);
						
	        		}else{
	        			System.out.format("dependency: %s is empty or does not exist.\n", dep);
	        		}
	        	}
	//        	parameters.put("dependences", cli.getOptionValue("dependences"));
	        }
	        if(cli.hasOption("buggylocDir")){
	        	String buggylocDir = cli.getOptionValue("buggylocDir");
	        	FileUtil.buggylocDir = buggylocDir;
	        	//FileUtil.flPath = new File(buggylocDir).getAbsolutePath() + "/FL.txt";
	        	FileUtil.buggylocPath = new File(buggylocDir).getAbsolutePath() + "/buggyloc.txt";
	        	
	        	String toolOutputDir = new File(buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName;
	        	
	        	// to be written
	        	FileUtil.flLogPath = toolOutputDir + "/fl_log.txt";
	        	FileUtil.writeToFile(FileUtil.flLogPath, "", false);
	        	
//	        	FileUtil.searchLogPath = toolOutputDir + "/search_log.txt";
//	        	FileUtil.writeToFile(FileUtil.searchLogPath, "", false); // init
	        	
//	        	FileUtil.changedFLPath = toolOutputDir + "/changedFL.txt";
//	        	FileUtil.writeToFile(FileUtil.changedFLPath, "", false); // init
	        	
	        	// save fl list for first fl. 
	        	FileUtil.oriFLPath = toolOutputDir + "/oriFL.txt";
	        	FileUtil.oriFlLogPath = toolOutputDir + "/oriFL.log";	        	
	        	FileUtil.writeToFile(FileUtil.oriFLPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.oriFlLogPath, "", false); 
	        	
	        	// second fl
	        	FileUtil.filteredFLPath = toolOutputDir + "/filteredFL.txt";
	        	FileUtil.filteredFlLogPath = toolOutputDir + "/filteredFL.log";
	        	FileUtil.writeToFile(FileUtil.filteredFLPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.filteredFlLogPath, "", false); 
	        	
//	        	FileUtil.positiveTestsPath = toolOutputDir + "/allPosTests.txt";
//	        	FileUtil.filteredPositiveTestsPath = toolOutputDir + "/filteredPosTests.txt";
//	        	FileUtil.writeToFile(FileUtil.positiveTestsPath, "", false); // init
//	        	FileUtil.writeToFile(FileUtil.filteredPositiveTestsPath, "", false); // init
	        }
	        if(cli.hasOption("externalProjPath")){
	        	FileUtil.externalProjPath = cli.getOptionValue("externalProjPath");
	        }
	        if(cli.hasOption("jvmPath")){
	        	try {
					FileUtil.jvmPath = new File(cli.getOptionValue("jvmPath")).getCanonicalPath() + "/java";
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if(cli.hasOption("failedTestsStr")){
	        	FileUtil.failedTestsStr = cli.getOptionValue("failedTestsStr");
	        	FileUtil.oriFailedTests = Arrays.asList(FileUtil.failedTestsStr.split(":"));
	        }
	        if(cli.hasOption("gzoltarDir")){
	        	FileUtil.gzoltarDir = new File(cli.getOptionValue("gzoltarDir")).getAbsolutePath();
	        }
//	        if(cli.hasOption("bugDir")){
//	        	FileUtil.bugDir = cli.getOptionValue("bugDir");
//	        }
//	        if(cli.hasOption("junitJar")){
//	        	FileUtil.junitJar = cli.getOptionValue("junitJar");
//	        }
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        // make sure that src/ test-classes/ src-classes/ are contained
     	if( ! FileUtil.depsList.contains(FileUtil.srcJavaDir)){
     		FileUtil.depsList.add(FileUtil.srcJavaDir);
     	}
     	if( ! FileUtil.depsList.contains(FileUtil.binJavaDir)){
     		FileUtil.depsList.add(FileUtil.binJavaDir);
     	}
     	if( ! FileUtil.depsList.contains(FileUtil.binTestDir)){
     		FileUtil.depsList.add(FileUtil.binTestDir);
     	}
        
//		return parameters;
    }
	
//	private static void replicateTests(Set<String> testClasses) {
//		// write to file.
//		List<String> allTests = new ArrayList<>();
//		for (String test : testClasses){
//			allTests.add(test);
//		}
//		
//		allTests.removeAll(FileUtil.oriFailedTests);
//		FileUtil.writeLinesToFile(FileUtil.positiveTestsPath, allTests);
//				
//		// run failed tests
//		long startT = System.currentTimeMillis();
//		PatchTest pt = new PatchTest(Arrays.asList(FileUtil.failedTestsStr.split(":")));
//		Boolean testResult = pt.runTests();
//		List<String> failedAfterTest = pt.getFailedTests();		
//		List<String> failedAfterTestCopy = pt.getFailedTests();
//		FileUtil.writeToFile(FileUtil.flLogPath, String.format("Time cost of pre-process before patch generation/validation (run all failed tests): %s\n", FileUtil.countTime(startT)) );
//		
//		FileUtil.writeToFile(String.format("oriFailedTests size: %d, replicated failed tests size: %d\n", FileUtil.oriFailedTests.size(),
//				failedAfterTestCopy.size()));
//		
//		if (failedAfterTest.isEmpty()){
//				System.err.println("No failed tests found in failed tests result replication.\n");
//				FileUtil.writeToFile("No failed tests found in failed tests result replication.\n");
//				System.exit(0);
//		}
//				
//		failedAfterTest.retainAll(FileUtil.oriFailedTests);
//		if (failedAfterTest.size() != FileUtil.oriFailedTests.size()){ // the same failed test (replication/reproduction)
//			FileUtil.writeToFile("replication (failed tests) failed.\n");
//			for (String test : failedAfterTestCopy){
//				FileUtil.writeToFile(String.format("replicated failed test: %s\n", test));
//			}
//			for (String test : failedAfterTestCopy){
//				FileUtil.writeToFile(String.format("original failed test: %s\n", test));
//			}
//			System.exit(0);
//		}else{
//			// run positive tests
//			startT = System.currentTimeMillis();
//			pt = new PatchTest(FileUtil.positiveTestsPath);
//			testResult = pt.runTests();
//			failedAfterTest = pt.getFailedTests();
//			FileUtil.writeToFile(FileUtil.flLogPath, String.format("Time cost of pre-process before patch generation/validation (run all positive tests): %s\n", FileUtil.countTime(startT)) );
//			
//			if (failedAfterTest.isEmpty()){
//				FileUtil.writeToFile("replication (all tests) passed.\n");
//			}else{
//				FileUtil.writeToFile("replication (pos tests) failed.\n");
//				for (String test : failedAfterTest){
//					FileUtil.writeToFile(String.format("failed pos test: %s\n", test));
//					FileUtil.fakedPosTests.add(test);
//				}
//						
//				allTests.removeAll(FileUtil.fakedPosTests);
//				FileUtil.writeLinesToFile(FileUtil.filteredPositiveTestsPath, allTests);
//				}
//			}
//	}
	
	/**
	 * @Description replicate tests given by testClasses
	 * @author apr
	 * @version Apr 8, 2020
	 *
	 */
	private static void replicateTests(String testPath) {
		// run all test methods
		String savePath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/failedMethods_replicate.txt";
		PatchTest pt = new PatchTest(testPath, false, savePath); // do not run test methods, just run tests. So false.
		pt.runTests();
//		List<String> failedMethodsAfterTest = pt.getFailedTestMethods();
		List<String> failedMethodsAfterTest = FileUtil.readFile(savePath);
		
		// check if there is extra tests
		int fakeCnt = 0;
		for (String failedMethod : failedMethodsAfterTest){
			if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
				FileUtil.fakedPosTests.add(failedMethod);
				fakeCnt ++;
				FileUtil.writeToFile(String.format("fake pos test method: %s\n", failedMethod));
			}
		}
		
		FileUtil.writeToFile(String.format("fakeCnt: %s\n", fakeCnt));
		// check if there is any expected failed test.
		if (fakeCnt == failedMethodsAfterTest.size()){
			FileUtil.writeToFile("[replicateTests] expected failed tests are not found. Exit now.\n");
			System.exit(0);
		}
	}
	
	/**
	 * @Description replicate tests according to the test methods listed by gzoltar v1.7.3 
	 * @author apr
	 * @version Apr 4, 2020
	 *
	 */
	private static void replicateTests() {
		// get all test methods
		String savePath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/failedMethods_replicate.txt";
		String unitPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/unit_tests.txt";
		List<String> testMethods = FileUtil.readTestMethodFile(unitPath);
		String testMethodsPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/test_methods.txt";
		FileUtil.writeLinesToFile(testMethodsPath, testMethods, false);
		
		// run all test methods
		PatchTest pt = new PatchTest(testMethodsPath, true, savePath);
		pt.runTests();
		List<String> failedMethodsAfterTest = pt.getFailedTestMethods();
		
		// check if there is extra tests
		int fakeCnt = 0;
		for (String failedMethod : failedMethodsAfterTest){
			if( ! FileUtil.oriFailedTests.contains(failedMethod.split("#")[0])){
				FileUtil.fakedPosTests.add(failedMethod);
				fakeCnt ++;
				FileUtil.writeToFile(String.format("fake pos test method: %s\n", failedMethod));
			}
		}
		
		FileUtil.writeToFile(String.format("fakeCnt: %s\n", fakeCnt));
		if (fakeCnt == failedMethodsAfterTest.size()){
			FileUtil.writeToFile("expected failed tests are not found. Exit now.\n");
			System.exit(0);
		}
		
//		testMethods.removeAll(FileUtil.fakedPosTests);
//		String nonFakePosTestPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/non_fake_pos_tests.txt";
//		FileUtil.writeLinesToFile(nonFakePosTestPath, testMethods, false);
	}
}
