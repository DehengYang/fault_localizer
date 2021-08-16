package apr.module.fl.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import apr.module.fl.execute.PatchTest;
import apr.module.fl.global.Globals;
import apr.module.fl.localization.FaultLocalizer;
import apr.module.fl.localization.FaultLocalizer2;
import apr.module.fl.localization.FaultLocalizerNopol;
import apr.module.fl.utils.ClassFinder;
import apr.module.fl.utils.FileUtil;

public class Main {
    final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // get args
        parseCommandLine(args);

        // get src & test clasess, and then save them to file
        ClassFinder cf = new ClassFinder();
        Set<String> testClasses = cf.getTestClasses(Globals.binTestDir, Globals.binJavaDir,
                Globals.depList);
        /*
         * I cannot exactly remember why I use "java" suffix as the filter rather than "class" just like testClasses.
         * Now QuixBugs expose this problem. It's src class has extra package: javaprograms, but has no corresponding folder.
         * Therefore, I decide to use "class" filter to find all src classes.
         * I now understand why. Refer to: /home/apr/apr_tools/automated-program-repair/apr/src/test/java/apr.module.fl/utils/ClassFinderTest.java, which may help us understand it better.
         * Closure 18 has src classes in test classes dir.
         * Actually, I don't need to use "java" filter, as this is not my fault, this is the fault of Closure which compiles src classes into test classesDir.
         * Therefore, there is no need for me to change code for these exceptional cases, which, I reckon, only accounts for a small percentage.
         * So, finally I decide to preserve the "java" filter method. But do not use it in the next fl.
         */
        Set<String> srcClassesFromSrcDir = cf.getJavaClassesOldVersion(Globals.srcJavaDir, "java");
        Set<String> srcClasses = cf.getJavaClasses(Globals.binJavaDir, Globals.depList);
        String testClassesPath = new File(Globals.workingDir).getAbsolutePath() + "/testClasses.txt";
        String srcClassesPath = new File(Globals.workingDir).getAbsolutePath() + "/srcClasses.txt";
        String srcClassesFromSrcDirPath = new File(Globals.workingDir).getAbsolutePath()
                + "/srcClassesFromSrcDir.txt";
        FileUtil.writeLinesToFile(srcClassesPath, srcClasses);
        FileUtil.writeLinesToFile(srcClassesFromSrcDirPath, srcClassesFromSrcDir);
        FileUtil.writeLinesToFile(testClassesPath, testClasses);

        // fault localization v0.1.1
        faultLocalize(testClasses, srcClasses);

        // fault localization v0.1.1 from nopol
        faultLocalizeNopol(testClasses, srcClasses);

        // fl v1.7.3
        faultLocalize2(testClasses, srcClasses);
        // old version, not used now.
        // long startTime = System.currentTimeMillis();
        // FaultLocalizer2 fl = new FaultLocalizer2();
        //// fl.localize();
        //// fl.logFL(true); // simplify
        //// fl.logFL(); // no simplification
        // FileUtil.writeToFile(String.format("fl (v1.7.3) time cost: %s\n", FileUtil.countTime(startTime)));
        //// System.exit(0);

        // replicate all tests
        // replicateTests(testClasses); // old version, not used now
        replicateTests(testClassesPath);

        // read fl results from file
        // FaultLocalizer fl = new FaultLocalizer();
        // List<SuspiciousLocation> suspList = fl.readFLResults(FileUtil.flPath);
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
        // fl.logFL(true); // simplify [not used now]
        fl.logFL(); // no simplification
        fl.getCoveredStmtsInfo();
        List<String> failedMethods = fl.getFailedMethods();
        FileUtil.writeToFile(String.format("[faultLocalize2] [time cost] of first fl (v1.7.3): %s\n",
                FileUtil.countTime(startTime)));

        // get extra failed cases (copy from replicate, and then modify)
        List<String> extraFailedMethods = new ArrayList<>();
        for (String failedMethod : failedMethods) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                extraFailedMethods.add(failedMethod);
                FileUtil.writeToFile(String.format(
                        "[faultLocalize2] First fl (v1.7.3) extra failed test method: %s\n", failedMethod));
            }
        }
        FileUtil.writeToFile(
                String.format("[faultLocalize2] First fl (v1.7.3) extra failed test method count: %s\n",
                        extraFailedMethods.size()));
        // check if there is any expected failed test.
        if (extraFailedMethods.size() == failedMethods.size()) {
            FileUtil.writeToFile(
                    "[faultLocalize2] First fl (v1.7.3) no expected failed tests are found. Exit now.\n");
            System.exit(0);
        }

        if (extraFailedMethods.isEmpty()) {
            return;
        }

        // else, start second fl run
        // get new unit_test file for second run
        String oriUnitTestsFile = new File(Globals.workingDir).getAbsolutePath() + "/FL/unit_tests.txt";
        String newUnitTestsFile = new File(Globals.workingDir).getAbsolutePath() + "/FL/new_unit_tests.txt";
        List<String> oriUnitTests = FileUtil.readTestFile(oriUnitTestsFile, false); // is not csv file -> false
        int oriUnitTestsSize = oriUnitTests.size();
        oriUnitTests.removeAll(extraFailedMethods);

        // extra check if extraFailedMethods are contained in oriUnitTests
        if (oriUnitTestsSize == oriUnitTests.size()) {
            FileUtil.writeToFile(
                    "[faultLocalize2] Second fl (v1.7.3) extraFailedMethods are not found in oriUnitTests. Exit now.\n");
            System.exit(0);
        }

        // write new unit_test file
        FileUtil.writeToFile(newUnitTestsFile, "", false); // init
        for (String unitTest : oriUnitTests) {
            FileUtil.writeToFile(newUnitTestsFile, "JUNIT," + unitTest + "\n");
        }

        // new fl
        startTime = System.currentTimeMillis();
        FaultLocalizer2 secondFl = new FaultLocalizer2(newUnitTestsFile);
        secondFl.localize();
        secondFl.logFL(); // no simplification
        secondFl.getCoveredStmtsInfo();
        List<String> secondFailedMethods = secondFl.getFailedMethods();
        List<String> secExtraFailedMethods = new ArrayList<>();
        FileUtil.writeToFile(String.format("[faultLocalize2] [time cost] of second fl (v1.7.3): %s\n",
                FileUtil.countTime(startTime)));

        for (String failedMethod : secondFailedMethods) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                secExtraFailedMethods.add(failedMethod);
                FileUtil.writeToFile(String.format(
                        "[faultLocalize2] Second fl (v1.7.3) extra failed test method: %s\n", failedMethod));
            }
        }
        FileUtil.writeToFile(
                String.format("[faultLocalize2] second fl (v1.7.3) extra failed test method count: %s\n",
                        secExtraFailedMethods.size()));

        if (secExtraFailedMethods.size() == secondFailedMethods.size()) {
            FileUtil.writeToFile(
                    "[faultLocalize2] second fl (v1.7.3) no expected failed tests are found. Exit now.\n");
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

        FaultLocalizer fl = new FaultLocalizer(Globals.oriFLPath, Globals.oriFlLogPath, testClasses,
                srcClasses);
        fl.localize();
        List<String> failedMethods = fl.getFailedMethods();

        List<String> extraFailedMethods = new ArrayList<>();
        for (String failedMethod : failedMethods) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                extraFailedMethods.add(failedMethod);
                FileUtil.writeToFile(String.format(
                        "[faultLocalize] First fl (v0.1.1) extra failed test method: %s\n", failedMethod));
            }
        }
        FileUtil.writeToFile(
                String.format("[faultLocalize] First fl (v0.1.1) extra failed test method count: %s\n",
                        extraFailedMethods.size()));
        
        // check if there is any expected failed test.
        if (extraFailedMethods.size() == failedMethods.size()) {
            FileUtil.writeToFile(
                    "[faultLocalize] First fl (v0.1.1) no expected failed tests are found. Exit now.\n");
            System.exit(0);
        }

        FileUtil.writeToFile(String.format("[faultLocalize] [time cost] of first fl (v0.1.1): %s\n",
                FileUtil.countTime(startTime)));
    }

    /**
     * @Description this is to support another gz implementation. This is mainly due to the fact that 
     * `/mnt/benchmarks/repairResults/Bugs.jar/Accumulo/df4b1985/APR/0/repair.log` hangs on at gz 0.1.1 
     * @author apr
     * @version Apr 15, 2020
     *
     * @param args
     */
    private static void faultLocalizeNopol(Set<String> testClasses, Set<String> srcClasses) {
        long startTime = System.currentTimeMillis();

        FaultLocalizerNopol fl = FaultLocalizerNopol.createInstance(Globals.oriFLPath, Globals.oriFlLogPath,
                testClasses, srcClasses);
        List<String> failedMethods = fl.getFailedMethods();

        List<String> extraFailedMethods = new ArrayList<>();
        for (String failedMethod : failedMethods) {
            if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                extraFailedMethods.add(failedMethod);
                FileUtil.writeToFile(String.format(
                        "[faultLocalize] First fl (v0.1.1) extra failed test method: %s\n", failedMethod));
            }
        }
        FileUtil.writeToFile(
                String.format("[faultLocalize] First fl (v0.1.1) extra failed test method count: %s\n",
                        extraFailedMethods.size()));
        // check if there is any expected failed test.
        if (extraFailedMethods.size() == failedMethods.size()) {
            FileUtil.writeToFile(
                    "[faultLocalize] First fl (v0.1.1) no expected failed tests are found. Exit now.\n");
            System.exit(0);
        }

        FileUtil.writeToFile(String.format("[faultLocalize] [time cost] of first fl (v0.1.1): %s\n",
                FileUtil.countTime(startTime)));

        if (!extraFailedMethods.isEmpty()) {
            startTime = System.currentTimeMillis();

            // logger.info("re-run fl due to {} extra failed test(s) in current FL.", extraFailedTests.size());
            FaultLocalizerNopol flSecond = FaultLocalizerNopol.createInstance(Globals.filteredFLPath,
                    Globals.filteredFlLogPath, testClasses, srcClasses, new HashSet<>(extraFailedMethods));
            List<String> secFailedMethods = flSecond.getFailedMethods();
            List<String> secExtraFailedMethods = new ArrayList<>();
            for (String failedMethod : secFailedMethods) {
                if (!Globals.oriFailedTestList.contains(failedMethod.split("#")[0])) {
                    secExtraFailedMethods.add(failedMethod);
                    FileUtil.writeToFile(
                            String.format("[faultLocalize] Second fl (v0.1.1) extra failed test method: %s\n",
                                    failedMethod));
                }
            }
            FileUtil.writeToFile(
                    String.format("[faultLocalize] Second fl (v0.1.1) extra failed test method count: %s\n",
                            secExtraFailedMethods.size()));
            // check if there is any expected failed test.
            if (secExtraFailedMethods.size() == secFailedMethods.size()) {
                FileUtil.writeToFile(
                        "[faultLocalize] Second fl (v0.1.1) no expected failed tests are found. Exit now.\n");
                System.exit(0);
            }

            FileUtil.writeToFile(String.format("[faultLocalize] [time cost] of second fl (v0.1.1): %s\n",
                    FileUtil.countTime(startTime)));
        }
    }

    // private static void replicateTests(Set<String> testClasses) {
    // // write to file.
    // List<String> allTests = new ArrayList<>();
    // for (String test : testClasses){
    // allTests.add(test);
    // }
    //
    // allTests.removeAll(FileUtil.oriFailedTests);
    // FileUtil.writeLinesToFile(FileUtil.positiveTestsPath, allTests);
    //
    // // run failed tests
    // long startT = System.currentTimeMillis();
    // PatchTest pt = new PatchTest(Arrays.asList(FileUtil.failedTestsStr.split(":")));
    // Boolean testResult = pt.runTests();
    // List<String> failedAfterTest = pt.getFailedTests();
    // List<String> failedAfterTestCopy = pt.getFailedTests();
    // FileUtil.writeToFile(FileUtil.flLogPath, String.format("Time cost of pre-process before patch generation/validation (run all failed tests): %s\n", FileUtil.countTime(startT)) );
    //
    // FileUtil.writeToFile(String.format("oriFailedTests size: %d, replicated failed tests size: %d\n", FileUtil.oriFailedTests.size(),
    // failedAfterTestCopy.size()));
    //
    // if (failedAfterTest.isEmpty()){
    // System.err.println("No failed tests found in failed tests result replication.\n");
    // FileUtil.writeToFile("No failed tests found in failed tests result replication.\n");
    // System.exit(0);
    // }
    //
    // failedAfterTest.retainAll(FileUtil.oriFailedTests);
    // if (failedAfterTest.size() != FileUtil.oriFailedTests.size()){ // the same failed test (replication/reproduction)
    // FileUtil.writeToFile("replication (failed tests) failed.\n");
    // for (String test : failedAfterTestCopy){
    // FileUtil.writeToFile(String.format("replicated failed test: %s\n", test));
    // }
    // for (String test : failedAfterTestCopy){
    // FileUtil.writeToFile(String.format("original failed test: %s\n", test));
    // }
    // System.exit(0);
    // }else{
    // // run positive tests
    // startT = System.currentTimeMillis();
    // pt = new PatchTest(FileUtil.positiveTestsPath);
    // testResult = pt.runTests();
    // failedAfterTest = pt.getFailedTests();
    // FileUtil.writeToFile(FileUtil.flLogPath, String.format("Time cost of pre-process before patch generation/validation (run all positive tests): %s\n", FileUtil.countTime(startT)) );
    //
    // if (failedAfterTest.isEmpty()){
    // FileUtil.writeToFile("replication (all tests) passed.\n");
    // }else{
    // FileUtil.writeToFile("replication (pos tests) failed.\n");
    // for (String test : failedAfterTest){
    // FileUtil.writeToFile(String.format("failed pos test: %s\n", test));
    // FileUtil.fakedPosTests.add(test);
    // }
    //
    // allTests.removeAll(FileUtil.fakedPosTests);
    // FileUtil.writeLinesToFile(FileUtil.filteredPositiveTestsPath, allTests);
    // }
    // }
    // }

    /**
     * @Description replicate tests given by testClasses
     * @author apr
     * @version Apr 8, 2020
     *
     */
    private static void replicateTests(String testPath) {
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
    private static void replicateTests() {
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

    /*
     * receive parameters
     */
    private static void parseCommandLine(String[] args) {
        Options options = new Options();
        options.addRequiredOption("sjd", "srcJavaDir", true,
                "src folder of the buggy program (e.g., /mnt/benchmarks/repairDir/Defects4J_Mockito_10/src)");
        options.addRequiredOption("bjd", "binJavaDir", true,
                "bin folder of the buggy program (e.g., /mnt/benchmarks/repairDir/Defects4J_Mockito_10/build/classes/main/)");
        options.addRequiredOption("btd", "binTestDir", true,
                "bin test folder of the buggy program (e.g., /mnt/benchmarks/repairDir/Defects4J_Mockito_10/build/classes/test/ )");
        options.addRequiredOption("dep", "dependencies", true,
                "all dependencies (i.e., classpath)");
        options.addRequiredOption("wd", "workingDir", true,
                "path of the buggy program (e.g., /mnt/benchmarks/repairDir/Defects4J_Mockito_10/)");
        options.addRequiredOption("jp", "jvmPath", true,
                "java path to run junit tests (e.g.,  /home/apr/env/jdk1.7.0_80/jre/bin/java)");
        options.addRequiredOption("ft", "failedTests", true,
                "expected bug triggering test(s) of the buggy program (e.g., com.google.javascript.jscomp.CollapseVariableDeclarationsTest)");
        options.addOption("to", "timeout", true, "time budget (in minutes).");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage() + "\n");
            formatter.printHelp(">>>>>>>>>> fault localization: \n\n", options);

            System.exit(1);
        }

        Globals.srcJavaDir = cmd.getOptionValue("srcJavaDir");
        Globals.binJavaDir = cmd.getOptionValue("binJavaDir");
        Globals.binTestDir = cmd.getOptionValue("binTestDir");
        Globals.dependencies = cmd.getOptionValue("dependencies");
        Globals.jvmPath = cmd.getOptionValue("jvmPath");
        Globals.failedTests = cmd.getOptionValue("failedTests");
        Globals.workingDir = cmd.getOptionValue("workingDir");
        if (cmd.hasOption("timeout")) {
            Globals.timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
        } 

        // post actions
        Globals.depList.addAll(Arrays.asList(Globals.dependencies.split(":")));

        Globals.oriFailedTestList = Arrays.asList(Globals.failedTests.split(":"));

        // save fl list for first fl.
        String toolOutputDir = new File(Globals.workingDir).getAbsolutePath();
        Globals.oriFLPath = toolOutputDir + "/oriFL.txt";
        Globals.oriFlLogPath = toolOutputDir + "/oriFL.log";
        FileUtil.writeToFile(Globals.oriFLPath, "", false); // init
        FileUtil.writeToFile(Globals.oriFlLogPath, "", false);

        // second fl
        Globals.filteredFLPath = toolOutputDir + "/filteredFL.txt";
        Globals.filteredFlLogPath = toolOutputDir + "/filteredFL.log";
        FileUtil.writeToFile(Globals.filteredFLPath, "", false); // init
        FileUtil.writeToFile(Globals.filteredFlLogPath, "", false);

        Globals.flLogPath = toolOutputDir + "/fl_log.txt";
        FileUtil.writeToFile(Globals.flLogPath, "", false);

    }

}
