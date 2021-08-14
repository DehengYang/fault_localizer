package apr.module.fl.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ClassFinderTest {
	@Test
	public void junit4FinderTest() throws IOException{
		// junit4 test dir
		String testDir = "/home/apr/apr_tools/nopol-experiment/nopol/nopol/target/test-classes";
		String srcDir = "/home/apr/apr_tools/nopol-experiment/nopol/nopol/target/classes";
		String depsPath = "/home/apr/apr_tools/nopol-experiment/nopol/nopol/target/dependency/";
		
		Collection<File> files = FileUtils.listFiles(new File(depsPath), new String[]{"jar"}, true);
		List<String> deps = new ArrayList<>();
		for (File file : files){
			deps.add(file.getCanonicalPath());
		}
		
		
		ClassFinder cf = new ClassFinder();
		
		cf.getTestClasses(testDir, srcDir, deps);
	}
	
	@Test
	public void junit3FinderTest() throws IOException{
		// junit4 test dir
		String testDir = "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/build/test/";
		String srcDir = "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/build/classes/";
		String depsPath = "/home/apr/apr_tools/nopol-experiment/nopol/nopol/target/dependency/";
		
		String deps = "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/build/classes:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/build/test:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/build/lib/rhino.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/args4j.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/junit.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/json.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/ant-launcher.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/jarjar.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/jsr305.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/protobuf-java.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/ant.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/guava.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/caja-r4314.jar:/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/lib/rhino/testsrc/org/mozilla/javascript/tests/commonjs/module/modules.jar";
		
		ClassFinder cf = new ClassFinder();
		
		Set<String> testClasses = cf.getTestClasses(testDir, srcDir, Arrays.asList(deps.split(":")));
	
		Set<String> testMethods = cf.getTestMethods();
		
		FileUtil.writeToFile("/tmp/clo18-apr-tests.txt", "", false);
		FileUtil.writeLinesToFile("/tmp/clo18-apr-tests.txt", testClasses, false);
		
		List<String> d4j = FileUtil.readFile("/home/apr/d4j/1");
		
		System.out.format("d4j size: %s, testClasses size: %s \n", d4j.size(), testClasses.size());
		
		for (String d : d4j){
			if(! testClasses.contains(d)){
				System.out.format("d4j test: %s is not included in testClasses\n", d);
			}
		}
		
		for (String d : testClasses){
			if(! d4j.contains(d)){
				System.out.format("testClasses test: %s is not included in d4j\n", d);
			}
		}
		
		FileUtil.writeToFile("/tmp/clo18-apr-testMethods.txt", "", false);
		FileUtil.writeLinesToFile("/tmp/clo18-apr-testMethods.txt", testMethods, false);
		
//		testClasses test: com.google.javascript.jscomp.RescopeGlobalSymbolsTest$StringCompare is not included in d4j
//		testClasses test: com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest$StringCompareTestCase is not included in d4j
//		testClasses test: com.google.javascript.jscomp.InlineFunctionsTest$StringCompare is not included in d4j
		
		// test getJavaClasses
		String javaSrcDir = "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/";
		Set<String> javaClasses1 = cf.getJavaClassesOldVersion(javaSrcDir, "java");
		Set<String> javaClasses2 = cf.getJavaClassesOldVersion(srcDir); //"class"
		
//		System.out.println(javaClasses1.removeAll(javaClasses2));
//		System.out.println();
//		System.out.println(javaClasses2.removeAll(javaClasses1));
		for (String clazz : javaClasses1){
			if(!javaClasses2.contains(clazz)){
				System.out.println(clazz + " is not contained in javaClasses2");
			}
		}
		System.out.println("---");
		for (String clazz : javaClasses2){
			if(!javaClasses1.contains(clazz)){
				System.out.println(clazz + " is not contained in javaClasses1");
			}
		}
		// this is okay if both size are not equal.
		// I prefer to use "class" filter rather than "java" filter.
		
		assertEquals(javaClasses1.size(), javaClasses2.size());
		
		javaClasses1.retainAll(javaClasses2);
		assertEquals(javaClasses1.size(), javaClasses2.size());
		
	}
}
