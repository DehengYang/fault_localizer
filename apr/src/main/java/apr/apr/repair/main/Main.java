package apr.apr.repair.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import apr.apr.repair.localization.FaultLocalizer;
import apr.apr.repair.localization.SuspiciousLocation;
import apr.apr.repair.parser.AttemptFileParser;
import apr.apr.repair.parser.ClassNode;
import apr.apr.repair.parser.ClassVarParser;
import apr.apr.repair.parser.CodeFragment;
import apr.apr.repair.parser.NodeFinder;
import apr.apr.repair.utils.ClassFinder;
import apr.apr.repair.utils.FileUtil;

public class Main {
	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args){
		// get parameters
		setParameters(args);
		
		// get src & test clasess
		ClassFinder cf = new ClassFinder();
		Set<String> testClasses = cf.getTestClasses(FileUtil.binTestDir, FileUtil.binJavaDir, FileUtil.depsList);
		Set<String> srcClasses = cf.getJavaClasses(FileUtil.srcJavaDir, "java");
		
		// fault localization
//		faultLocalize(testClasses, srcClasses);
		
		// read fl results from file
		FaultLocalizer fl = new FaultLocalizer();
		List<SuspiciousLocation> suspList = fl.readFLResults();
		
		// parse java files into ast
//		for(String srcClass : srcClasses){
//			AttemptFileParser fp = new AttemptFileParser(srcClass, FileUtil.srcJavaDir);
////			codeFinder.parse(srcClass, FileUtil.srcJavaDir, 88); //70, 215, 214, 64, 59, 60, 312, 382, 87
//			// /mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/com/google/javascript/jscomp/CoalesceVariableNames.java
//			break;
//		}
		ClassVarParser cvp =  new ClassVarParser(new ArrayList<>(srcClasses), FileUtil.srcJavaDir);
		Map<String, ClassNode> classVarMap = cvp.getClassVarMap();
//		cvp.printClassVarMap();
		
		// get/list all variables for the given file
//		NodeFinder sf = new NodeFinder(78, "com.google.javascript.jscomp.CoalesceVariableNames", 
//				"/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/", "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"); 
//		sf.getAllVariables(classVarMap);
//		sf.printVars();
		
		repairLocations(suspList);
	}

	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param suspList
	 */
	private static void repairLocations(List<SuspiciousLocation> suspList) {
		for (SuspiciousLocation sl : suspList){
			CodeFragment cf = new CodeFragment(sl.getLineNo(), sl.getClassName(), FileUtil.srcJavaDir);
		}
		
	}

	/** @Description fault localization & re-fl if extra failed tests found
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 */
	private static void faultLocalize(Set<String> testClasses, Set<String> srcClasses) {
		FaultLocalizer fl = new FaultLocalizer(FileUtil.oriFLPath, testClasses, srcClasses);
		Set<String> extraFailedTests = fl.getExtraFailedTests(FileUtil.oriFailedTests);
		if (! extraFailedTests.isEmpty()){
			logger.info("re-run fl due to {} extra failed test(s) in current FL.", extraFailedTests.size());
			FaultLocalizer flSecond = new FaultLocalizer(FileUtil.filteredFLPath, testClasses, srcClasses, new ArrayList<>(extraFailedTests));
			Set<String> reExtraFailedTests = flSecond.getExtraFailedTests(FileUtil.oriFailedTests);
		
			if (! reExtraFailedTests.isEmpty()){
				logger.warn("After re-running fl, there still exists {} extra failed test(s) in current FL.", reExtraFailedTests.size());
			}
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
        opt4.setRequired(true);
        Option opt6 = new Option("externalProjPath","externalProjPath",true,"Path to run junit tests. e.g., /home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        opt4.setRequired(true);
        Option opt7 = new Option("jvmPath","jvmPath",true,"java path to run junit tests (e.g., /home/apr/env/jdk1.7.0_80/jre/bin/java)");
        opt4.setRequired(true);
        Option opt8 = new Option("failedTestsStr","failedTestsStr",true,"e.g., com.google.javascript.jscomp.CollapseVariableDeclarationsTest");
        opt4.setRequired(true);

        Options options = new Options();
        options.addOption(opt1);
        options.addOption(opt2);
        options.addOption(opt3);
        options.addOption(opt4);
        options.addOption(opt5);
        options.addOption(opt6);
        options.addOption(opt7);
        options.addOption(opt8);

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
	        			System.out.format("dependency: %s is empty or does not exist.", dep);
	        		}
	        	}
	//        	parameters.put("dependences", cli.getOptionValue("dependences"));
	        }
	        if(cli.hasOption("buggylocDir")){
	        	String buggylocDir = cli.getOptionValue("buggylocDir");
	        	FileUtil.buggylocDir = buggylocDir;
	        	FileUtil.flPath = new File(buggylocDir).getAbsolutePath() + "/FL.txt";
	        	FileUtil.buggylocPath = new File(buggylocDir).getAbsolutePath() + "/buggyloc.txt";
	        	
	        	// to be written
	        	FileUtil.flLogPath = new File(buggylocDir).getAbsolutePath() + "/fl_log_" + FileUtil.toolName + ".txt";
	        	FileUtil.searchLogPath = new File(buggylocDir).getAbsolutePath() + "/search_log_" + FileUtil.toolName + ".txt";
	        	FileUtil.changedFLPath = new File(buggylocDir).getAbsolutePath() + "/changedFL_" + FileUtil.toolName + ".txt";
	        	FileUtil.oriFLPath = new File(buggylocDir).getAbsolutePath() + "/oriFL_" + FileUtil.toolName + ".txt";
	        	FileUtil.filteredFLPath = new File(buggylocDir).getAbsolutePath() + "/filteredFL_" + FileUtil.toolName + ".txt";
	        	FileUtil.positiveTestsPath = new File(buggylocDir).getAbsolutePath() + "/allPosTests_" + FileUtil.toolName + ".txt";
	        	FileUtil.filteredPositiveTestsPath = new File(buggylocDir).getAbsolutePath() + "/filteredPosTests_" + FileUtil.toolName + ".txt";
	        	
	        	FileUtil.writeToFile(FileUtil.flLogPath, "", false);
	        	FileUtil.writeToFile(FileUtil.searchLogPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.changedFLPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.filteredFLPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.oriFLPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.positiveTestsPath, "", false); // init
	        	FileUtil.writeToFile(FileUtil.filteredPositiveTestsPath, "", false); // init
	        	
	//        	parameters.put("buggylocDir", cli.getOptionValue("buggylocDir"));
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
}
