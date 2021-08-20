/**
 * apr
 * Aug 16, 2021
 */
package apr.module.fl.execute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import apr.module.fl.global.Globals;
import apr.module.fl.utils.FileUtil;

/**
 * @author apr
 * Aug 16, 2021
 */
public class Replicate {

    final static Logger logger = LogManager.getLogger(Replicate.class);

    /**
     * @Description replicate tests given by testClasses
     * @author apr
     * @version Apr 8, 2020
     *
     */
    public static void replicateTests(String testPath) {
        List<String> testResults = FileUtil.readFile(testPath);
        List<String> testMethods = new ArrayList<>();
        for (String tr:testResults) {
            testMethods.add(tr.split(",")[0]);
        }
        String realTestPath = Globals.outputDir + "/test_methods.txt";
        FileUtil.writeLinesToFile(realTestPath, testMethods, false);
        
        // run all test methods
        String savePath = Globals.outputDir + "/all_failed_methods_replicate.txt";
        PatchTest pt = new PatchTest(savePath, Globals.jvmPath, Globals.externalProjPath,
                Globals.outputDir, Globals.binJavaDir, Globals.binTestDir, Globals.dependencies, null);
        pt.configure(realTestPath, true);
        List<String> failedMethodsAfterTest = pt.runTests();

        // check if there is extra tests
        int fakeCnt = 0;
        for (String failedMethod : failedMethodsAfterTest) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                Globals.fakedPosTests.add(failedMethod);
                fakeCnt++;
            } else {
                Globals.expectedFailedTests.add(failedMethod);
            }
        }

        FileUtil.writeToFile(String.format("[replicateTests] fakeCnt: %s\n", fakeCnt));
        FileUtil.writeLinesToFile(Globals.outputDir + "/expected_failed_test_replicate.txt",
                Globals.expectedFailedTests, false);
        FileUtil.writeLinesToFile(Globals.outputDir + "/extra_failed_test_replicate.txt",
                Globals.fakedPosTests, false);
        testMethods.removeAll(failedMethodsAfterTest);
        FileUtil.writeLinesToFile(Globals.outputDir + "/positive_test_replicate.txt",
                testMethods, false);
        // check if there is any expected failed test.
        if (fakeCnt == failedMethodsAfterTest.size()) {
            FileUtil.writeToFile("[replicateTests] expected failed tests are not found. Exit now.\n");
            System.exit(0);
        }
    }

    // not used
    // /**
    // * @Description replicate tests according to the test methods listed by gzoltar v1.7.3
    // * @author apr
    // * @version Apr 4, 2020
    // *
    // */
    // public static void replicateTests() {
    // // get all test methods
    // String savePath = new File(Globals.workingDir).getAbsolutePath() + "/FL/failedMethods_replicate.txt";
    // String unitPath = new File(Globals.workingDir).getAbsolutePath() + "/FL/unit_tests.txt";
    // List<String> testMethods = FileUtil.readTestMethodFile(unitPath);
    // String testMethodsPath = new File(Globals.workingDir).getAbsolutePath() + "/FL/test_methods.txt";
    // FileUtil.writeLinesToFile(testMethodsPath, testMethods, false);
    //
    // // run all test methods
    // PatchTest pt = new PatchTest(testMethodsPath, true, savePath);
    // pt.runTests();
    // List<String> failedMethodsAfterTest = pt.getFailedTestMethods();
    //
    // // check if there is extra tests
    // int fakeCnt = 0;
    // for (String failedMethod : failedMethodsAfterTest) {
    // if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
    // Globals.fakedPosTests.add(failedMethod);
    // fakeCnt++;
    // FileUtil.writeToFile(
    // String.format("[replicateTests] fake pos test method: %s\n", failedMethod));
    // }
    // }
    //
    // FileUtil.writeToFile(String.format("[replicateTests] fakeCnt: %s\n", fakeCnt));
    // if (fakeCnt == failedMethodsAfterTest.size()) {
    // FileUtil.writeToFile("[replicateTests] expected failed tests are not found. Exit now.\n");
    // System.exit(0);
    // }
    //
    // // testMethods.removeAll(FileUtil.fakedPosTests);
    // // String nonFakePosTestPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/non_fake_pos_tests.txt";
    // // FileUtil.writeLinesToFile(nonFakePosTestPath, testMethods, false);
    // }
}
