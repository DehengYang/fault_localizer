/**
 * apr
 * Aug 16, 2021
 */
package apr.module.fl.execute;

import java.io.File;
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
        // run all test methods
        String savePath = new File(Globals.workingDir).getAbsolutePath() + "/FL/failedMethods_replicate.txt";
        logger.info("replicateTests starts");
        PatchTest pt = new PatchTest(testPath, false, savePath); // do not run test methods, just run tests. So false.
        pt.runTests();
        // List<String> failedMethodsAfterTest = pt.getFailedTestMethods();
        List<String> failedMethodsAfterTest = FileUtil.readFile(savePath);

        logger.info("replicateTests ends");

        // check if there is extra tests
        int fakeCnt = 0;
        for (String failedMethod : failedMethodsAfterTest) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                Globals.fakedPosTests.add(failedMethod);
                fakeCnt++;
                FileUtil.writeToFile(
                        String.format("[replicateTests] fake pos test method: %s\n", failedMethod));
            }
        }

        FileUtil.writeToFile(String.format("[replicateTests] fakeCnt: %s\n", fakeCnt));
        // check if there is any expected failed test.
        if (fakeCnt == failedMethodsAfterTest.size()) {
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
    public static void replicateTests() {
        // get all test methods
        String savePath = new File(Globals.workingDir).getAbsolutePath() + "/FL/failedMethods_replicate.txt";
        String unitPath = new File(Globals.workingDir).getAbsolutePath() + "/FL/unit_tests.txt";
        List<String> testMethods = FileUtil.readTestMethodFile(unitPath);
        String testMethodsPath = new File(Globals.workingDir).getAbsolutePath() + "/FL/test_methods.txt";
        FileUtil.writeLinesToFile(testMethodsPath, testMethods, false);

        // run all test methods
        PatchTest pt = new PatchTest(testMethodsPath, true, savePath);
        pt.runTests();
        List<String> failedMethodsAfterTest = pt.getFailedTestMethods();

        // check if there is extra tests
        int fakeCnt = 0;
        for (String failedMethod : failedMethodsAfterTest) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                Globals.fakedPosTests.add(failedMethod);
                fakeCnt++;
                FileUtil.writeToFile(
                        String.format("[replicateTests] fake pos test method: %s\n", failedMethod));
            }
        }

        FileUtil.writeToFile(String.format("[replicateTests] fakeCnt: %s\n", fakeCnt));
        if (fakeCnt == failedMethodsAfterTest.size()) {
            FileUtil.writeToFile("[replicateTests] expected failed tests are not found. Exit now.\n");
            System.exit(0);
        }

        // testMethods.removeAll(FileUtil.fakedPosTests);
        // String nonFakePosTestPath = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL/non_fake_pos_tests.txt";
        // FileUtil.writeLinesToFile(nonFakePosTestPath, testMethods, false);
    }
}
