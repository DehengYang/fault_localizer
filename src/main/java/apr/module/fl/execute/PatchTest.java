package apr.module.fl.execute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import apr.module.fl.global.Globals;
import apr.module.fl.utils.CmdUtil;
import apr.module.fl.utils.FileUtil;

/*
 * to run tests of buggy program (invoking "PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar")
 */
public class PatchTest {
	String testFilePath;
	private List<String> testCases;
//	String jvmPath = "/home/apr/env/jdk1.7.0_80/jre/bin/java"; ///home/apr/env/jdk1.7.0_80/jre/bin/java
//	String externalProjRoot = "/home/apr/apr_tools/tbar-ori/TBar-dale/externel/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar"; ///home/apr/apr_tools/tbar-ori/TBar-dale/externel/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar
	private List<String> dependencies;
	private List<String> failedTests = new ArrayList<>();
	private List<String> failedTestMethods = new ArrayList<>();
	private String savePath;
	
	String flag; // file or str
	
	boolean runTestMethods = false;
	
	public PatchTest(String testFilePath, String savePath){
		this.testFilePath = testFilePath;
		flag = "file";
		this.savePath = savePath;
	}
	
	public PatchTest(List<String> testCases, String savePath){
		this.testCases = testCases;
		flag = "str";
		this.savePath = savePath;
	}
	
	public PatchTest(String testFilePath, boolean runTestMethods, String savePath){
		this.testFilePath = testFilePath;
		flag = "file";
		this.runTestMethods = runTestMethods;
		this.savePath = savePath;
	}
	
	public PatchTest(List<String> testCases, boolean runTestMethods, String savePath){
		this.testCases = testCases;
		flag = "str";
		this.runTestMethods = runTestMethods;
		this.savePath = savePath;
	}
	
	public void runTests(){
		String cmd = "";
		// add java
		cmd += Globals.jvmPath + " -cp ";
		
		// add external jar
		try {
			cmd += new File(Globals.externalProjPath).getCanonicalPath() + File.pathSeparator;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// for testing
//		FLUtil.binJavaDir = "/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/classes/";
//		FLUtil.binTestDir = "/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/test/";
//		dependencies = Arrays.asList("/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/lib/rhino.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/args4j.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/junit.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/json.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/ant-launcher.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/jarjar.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/jsr305.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/protobuf-java.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/ant.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/guava.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/caja-r4314.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/rhino/testsrc/org/mozilla/javascript/tests/commonjs/module/modules.jar".split(":"));
		
		// add src & test classes & dependencies
		if (!Globals.dependencies.contains(Globals.binJavaDir)){
			cmd += Globals.binJavaDir + File.pathSeparator;
		}
		if (!Globals.dependencies.contains(Globals.binTestDir)){
			cmd += Globals.binTestDir + File.pathSeparator;
		}
		cmd += Globals.dependencies; // + File.pathSeparator;//bug fix
//		for (String dep : FLUtil.dependences){
//			cmd += dep + File.pathSeparator;
//		}
		
		// add main class & corresponding parameter
		if (flag.equals("str")){
			cmd += " apr.junit.PatchTest -testStr ";
			
			for (String test : testCases){
				cmd += test.trim() + File.pathSeparator; 
			}
		}else if (flag.equals("file")){
			cmd += " apr.junit.PatchTest -testFile " + testFilePath;
		}else{
			System.out.format("unknown flag of PatchTest: %s\n", flag);
		}
		
		if (runTestMethods){
			cmd += " -runTestMethods true";
		}
		
		// save path
		cmd += " -savePath " + savePath + " >/dev/null 2>&1";
		
		// run cmd
		System.out.println(cmd);
//		FLUtil.writeToFile(String.format("cmd for execution: %s\n", cmd));
		// for debugging usage
//		cmd = "/home/apr/env/jdk1.7.0_80/bin/java -cp /mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/classes:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/test:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-legacy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/protobuf_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/google_common_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/ant_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-core.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/libtrunk_rhino_parser_jarjared.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/hamcrest-core-1.1.jar:/home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar apr.junit.PatchTest -testFile /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Closure_103/allPosTests_Dale_APR.txt";
//		cmd = "/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/classes:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/test:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-legacy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/protobuf_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/google_common_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/ant_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-core.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/libtrunk_rhino_parser_jarjared.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/hamcrest-core-1.1.jar:/home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar apr.junit.PatchTest -testFile /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Closure_103/allPosTests_Dale_APR.txt";
//		String output = CmdUtil.runCmd2(cmd);
//		String output = CmdUtil.runCmd(cmd);
		
		CmdUtil.runCmdNoOutput(cmd);
		
		// parse output
//		String[] lines = output.split("\n");
//		for (String line : lines){
//			// find a failed test case
//			String str = "[Patch test] failed test: ";
//			if (line.length() > str.length() && line.substring(0, str.length()).equals(str)){
//				failedTests.add(line.split(":")[1].trim());
//			}
//			
//			// [Patch test] failed test method
//			str = "[Patch test] failed test method: ";
//			if (line.length() > str.length() && line.substring(0, str.length()).equals(str)){
//				failedTestMethods.add(line.split(":")[1].trim());
//			}
//		}
		
//		return failedTests.isEmpty();
	}
	
	public List<String> getFailedTests(){
		return this.failedTests;
	}
	
	public List<String> getFailedTestMethods(){
		return this.failedTestMethods;
	}
}
