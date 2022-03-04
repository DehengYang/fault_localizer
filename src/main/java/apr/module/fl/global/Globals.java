/**
 * apr
 * Aug 15, 2021
 */
package apr.module.fl.global;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author apr
 * Aug 15, 2021
 */
public class Globals {
    // required
    public static String srcJavaDir;
    public static String binJavaDir;
    public static String binTestDir;
    public static String dependencies;
    public static String jvmPath;
    public static String failedTests;
    public static String workingDir;
    public static String outputDir;

    // optional
    // public static int timeout = 360; // in minutes

    //
    public static ArrayList<String> depList = new ArrayList<>();
    public static List<String> oriFailedTestList;

    // for replicateTests
    public static List<String> fakedPosTests = new ArrayList<>();
    public static List<String> expectedFailedTests = new ArrayList<>();
    public static String externalProjPath;

    // for fl
    public static String rankListPath;

    // matrix
    public static String coveragePath;
    // public static String matrixPath;
    public static String testListPath;
    public static String stmtListPath;

    // matrix again
    public static String matrixPathAgain;
    public static String testListPathAgain;
    public static String rankListPathAgain;

    // log
    public static String flLogPath;

    // output yaml
    public static Map<String, Object> outputData = new LinkedHashMap<>();
    public static String outputDataPath;
}
