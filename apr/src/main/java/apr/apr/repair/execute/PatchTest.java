package apr.apr.repair.execute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import apr.apr.repair.utils.CmdUtil;
import apr.apr.repair.utils.FileUtil;

/*
 * to run tests of buggy program (invoking "PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar")
 */
public class PatchTest {
	String testFilePath;
	List<String> testCases;
//	String jvmPath = "/home/apr/env/jdk1.7.0_80/jre/bin/java"; ///home/apr/env/jdk1.7.0_80/jre/bin/java
//	String externalProjRoot = "/home/apr/apr_tools/tbar-ori/TBar-dale/externel/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar"; ///home/apr/apr_tools/tbar-ori/TBar-dale/externel/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar
	List<String> dependencies;
	List<String> failedTests = new ArrayList<>();
	List<String> failedTestsMethods = new ArrayList<>();
	
	String flag; // file or str
	
	public PatchTest(String testFilePath){
		this.testFilePath = testFilePath;
		flag = "file";
	}
	
	public PatchTest(List<String> testCases){
		this.testCases = testCases;
		flag = "str";
	}
	
	public boolean runTests(){
		String cmd = "";
		// add java
		cmd += FileUtil.jvmPath + " -cp ";
		
		// for testing
//		FLUtil.binJavaDir = "/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/classes/";
//		FLUtil.binTestDir = "/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/test/";
//		dependencies = Arrays.asList("/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/build/lib/rhino.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/args4j.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/junit.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/json.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/ant-launcher.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/jarjar.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/jsr305.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/protobuf-java.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/ant.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/guava.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/caja-r4314.jar:/mnt/benchmarks/repairDir/Nopol_Defects4J_Closure_8/lib/rhino/testsrc/org/mozilla/javascript/tests/commonjs/module/modules.jar".split(":"));
		
		// add src & test classes & dependencies
		if (!FileUtil.dependencies.contains(FileUtil.binJavaDir)){
			cmd += FileUtil.binJavaDir + File.pathSeparator;
		}
		if (!FileUtil.dependencies.contains(FileUtil.binTestDir)){
			cmd += FileUtil.binTestDir + File.pathSeparator;
		}
		cmd += FileUtil.dependencies + File.pathSeparator;//bug fix
//		for (String dep : FLUtil.dependences){
//			cmd += dep + File.pathSeparator;
//		}
		
		// add external jar
		try {
			cmd += new File(FileUtil.externalProjPath).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		
		// run cmd
		System.out.println(cmd);
//		FLUtil.writeToFile(String.format("cmd for execution: %s\n", cmd));
		// for debugging usage
//		cmd = "/home/apr/env/jdk1.7.0_80/bin/java -cp /mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/classes:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/test:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-legacy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/protobuf_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/google_common_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/ant_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-core.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/libtrunk_rhino_parser_jarjared.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/hamcrest-core-1.1.jar:/home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar apr.junit.PatchTest -testFile /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Closure_103/allPosTests_Dale_APR.txt";
//		cmd = "/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/classes:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/build/test:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-legacy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/protobuf_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/google_common_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/ant_deploy.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/junit4-core.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/libtrunk_rhino_parser_jarjared.jar:/mnt/benchmarks/repairDir/TBar_Defects4J_Closure_103/lib/hamcrest-core-1.1.jar:/home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar apr.junit.PatchTest -testFile /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Closure_103/allPosTests_Dale_APR.txt";
//		String output = CmdUtil.runCmd2(cmd);
		String output = CmdUtil.runCmd(cmd);
		
		// parse output
		String[] lines = output.split("\n");
		for (String line : lines){
			// find a failed test case
			if (line.length() > 12 && line.substring(0, 12).equals("failed test:")){
				failedTests.add(line.split(":")[1].trim());
			}
		}
		
		return failedTests.isEmpty();
	}
	
	public List<String> getFailedTests(){
		return this.failedTests;
	}
}
