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

import com.gzoltar.core.GZoltar;

import apr.module.fl.execute.Replicate;
import apr.module.fl.global.Globals;
import apr.module.fl.localization.FaultLocalizer;
import apr.module.fl.utils.ClassFinder;
import apr.module.fl.utils.FileUtil;
import apr.module.fl.utils.YamlUtil;

public class Main {
    final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
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
        String testClassesPath = new File(Globals.workingDir).getAbsolutePath() + "/test_classes.txt";
        String srcClassesPath = new File(Globals.workingDir).getAbsolutePath() + "/src_classes.txt";
        String srcClassesFromSrcDirPath = new File(Globals.workingDir).getAbsolutePath()
                + "/src_classes_from_src_dir.txt";
        FileUtil.writeLinesToFile(srcClassesPath, srcClasses);
        FileUtil.writeLinesToFile(srcClassesFromSrcDirPath, srcClassesFromSrcDir);
        FileUtil.writeLinesToFile(testClassesPath, testClasses);
        
        Globals.outputData.put("time_cost_classes_collection_1", FileUtil.countTime(startTime));

        // fault localization v0.1.1
        faultLocalize(testClasses, srcClasses);
        
        // write data
        Globals.outputData.put("time_cost_in_total", FileUtil.countTime(startTime));
        YamlUtil.writeYaml(Globals.outputData, Globals.outputDataPath);
        
        // replicate all tests
        // Replicate.replicateTests(testClassesPath);

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
        FaultLocalizer fl = new FaultLocalizer(Globals.rankListPath, testClasses, srcClasses);
        GZoltar gz = fl.runGzoltar();
        Globals.outputData.put("time_cost_run_fl_2", FileUtil.countTime(startTime));
        
        startTime = System.currentTimeMillis();
        fl.calculateSusp(gz);
        Globals.outputData.put("time_cost_calculate_susp_3", FileUtil.countTime(startTime));
        
        startTime = System.currentTimeMillis();
        fl.calculateSuspAgain(gz);
        Globals.outputData.put("time_cost_calculate_susp_again_4", FileUtil.countTime(startTime));
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
        options.addRequiredOption("od", "outputDir", true,
                "directory to save the fl results.");
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
        Globals.outputDir = cmd.getOptionValue("outputDir");
        if (cmd.hasOption("timeout")) {
            Globals.timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
        }

        // post actions
        Globals.depList.addAll(Arrays.asList(Globals.dependencies.split(":")));

        Globals.oriFailedTestList = Arrays.asList(Globals.failedTests.split(":"));

        // save fl list for first fl.
        String toolOutputDir = new File(Globals.outputDir).getAbsolutePath();
        Globals.flLogPath = Paths.get(toolOutputDir, "fl.log").toString();
        Globals.rankListPath = Paths.get(toolOutputDir, "ranking_list.txt").toString();
        FileUtil.writeToFile(Globals.flLogPath, "", false);

        Globals.matrixPath = Paths.get(toolOutputDir, "matrix.txt").toString();
        Globals.testListPath = Paths.get(toolOutputDir, "test_method_list.txt").toString();
        Globals.stmtListPath = Paths.get(toolOutputDir, "stmt_list.txt").toString();

        Globals.matrixPathAgain = Paths.get(toolOutputDir, "matrix_again.txt").toString();
        Globals.testListPathAgain = Paths.get(toolOutputDir, "test_method_list_again.txt").toString();
        Globals.rankListPathAgain = Paths.get(toolOutputDir, "rank_list_again.txt").toString();

        Globals.outputDataPath = Paths.get(toolOutputDir, "output_data.yaml").toString();
    }
}
