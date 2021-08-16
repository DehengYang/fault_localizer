package apr.module.fl.main;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

        // replicate all tests
        // replicateTests(testClasses); // old version, not used now
        // replicateTests(testClassesPath);

        // read fl results from file
        // FaultLocalizer fl = new FaultLocalizer();
        // List<SuspiciousLocation> suspList = fl.readFLResults(FileUtil.flPath);
    }

    /** @Description fault localization & re-fl if extra failed tests found
     * @author apr
     * @version Mar 17, 2020
     *
     */
    private static void faultLocalize(Set<String> testClasses, Set<String> srcClasses) {
        long startTime = System.currentTimeMillis();

        FaultLocalizer fl = new FaultLocalizer(Globals.rankListPath, testClasses,
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
        Globals.flLogPath = Paths.get(toolOutputDir, "fl.log").toString();
        Globals.rankListPath = Paths.get(toolOutputDir, "ranking_list.txt").toString();
        FileUtil.writeToFile(Globals.flLogPath, "", false);
        FileUtil.writeToFile(Globals.rankListPath, "", false);

        Globals.matrixPath = Paths.get(toolOutputDir, "matrix.txt").toString();
        Globals.testListPath = Paths.get(toolOutputDir, "test_list.txt").toString();
        Globals.stmtListPath = Paths.get(toolOutputDir, "stmt_list.txt").toString();

    }

}
