/**
 * apr
 * Aug 15, 2021
 */
package apr.module.fl.global;

import java.util.ArrayList;
import java.util.List;

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
    public static String classpath;
    public static String jvmPath;
    public static String failedTests;
    public static String workingDir;

    // optional
    public static int timeout; // in minutes

    // 
    public static ArrayList<String> depList = new ArrayList<>();
    public static List<String> oriFailedTestList;
    public static String oriFLPath;
    public static String oriFlLogPath;
    public static String filteredFLPath;
    public static String filteredFlLogPath;
    
    // save all positive tests
    public static List<String> fakedPosTests = new ArrayList<>();
    public static String flLogPath;
    
    // for faultlocalizer2.java
    public static String gzoltarDir = "";
    public static String externalProjPath = "";
}
