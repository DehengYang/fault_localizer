package apr.module.fl.localization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;

import apr.module.fl.global.Globals;
import apr.module.fl.utils.FileUtil;

public class FaultLocalizer {
    private String workDir = System.getProperty("user.dir");
    final static Logger logger = LogManager.getLogger(FaultLocalizer.class);

    private int totalPassed = 0;
    private int totalFailed = 0;

    private List<String> failedMethods = new ArrayList<>();

    private List<SuspiciousLocation> suspList = new ArrayList<>();

    private Set<String> testClasses = new HashSet<>();
    private Set<String> srcClasses = new HashSet<>();
    private String savePath;

    private int[][] matrix;

    public FaultLocalizer() {

    }

    public FaultLocalizer(String savePath, Set<String> testClasses, Set<String> srcClasses) {
        // this(null, null);
        this.savePath = savePath;
        this.testClasses.addAll(testClasses);
        this.srcClasses.addAll(srcClasses);
        // localize(null);
    }

    public FaultLocalizer(String savePath, String logPath, Set<String> testClasses, Set<String> srcClasses,
            HashSet<String> extraFailedMethods) {
        this.savePath = savePath;
        this.testClasses.addAll(testClasses);
        this.srcClasses.addAll(srcClasses);
        localize(extraFailedMethods);
    }

    /** @Description 
     * @author apr
     * @version Mar 21, 2020
     *
     */
    public List<SuspiciousLocation> readFLResults(String flPath) {
        List<String> lines = FileUtil.readFile(flPath);
        List<SuspiciousLocation> suspList = new ArrayList<>();
        // com.google.javascript.rhino.Token:217,0.8164965809277261
        for (String line : lines) {
            String className = line.split(":")[0];
            int lineNo = Integer.parseInt(line.split(":")[1].split(",")[0]);
            double suspValue = Double.parseDouble(line.split(":")[1].split(",")[1]);
            suspList.add(new SuspiciousLocation(className, lineNo, suspValue));
        }
        return suspList;
    }

    public void localize() {
        localize(null);
    }

    /**
     * @Description this fl is different from that of nopol.
     * 1) this instruments the src classes found in the src classes dir, but nopol does not instruments junit test classes. This is a slight difference.
     * 2) this considers extra failed test methods.
     * 
     * Also, I learned from that: no software can avoid bugs. So we don't have to persue 100% perfect sometimes, especially in the experiments, which may cost huge time.
     * 
     * @author apr
     * @version Apr 12, 2020
     *
     * @param extraFailedMethods
     */
    public void localize(HashSet<String> extraFailedMethods) {
        logger.info("FL starts.");

        GZoltar gz = null;
        try {
            gz = new GZoltar(workDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set classpath
        gz.setClassPaths(Globals.depList);

        gz.addTestPackageNotToExecute("junit.framework"); // prevent extra unrelated failed tests
        gz.addTestPackageNotToExecute("org.junit");
        gz.addTestPackageNotToExecute("org.easymock");

        // seems does not work.
        gz.addTestPackageNotToExecute("junit.framework.TestSuite$1#warning");
        gz.addTestPackageNotToExecute("junit.framework.TestSuite$1");
        gz.addTestPackageNotToExecute("junit.framework.TestSuite");

        for (String testClass : testClasses) {
            if (testClass.contains("junit.framework")) {
                continue;
            }

            gz.addTestToExecute(testClass);
        }

        if (extraFailedMethods != null) {
            for (String extraFailedMethod : extraFailedMethods) {
                gz.addTestNotToExecute(extraFailedMethod);
            }
        }

        gz.addPackageNotToInstrument("org.junit");
        gz.addPackageNotToInstrument("junit.framework");
        gz.addPackageNotToInstrument("org.easymock");

        for (String srcClass : srcClasses) {
            gz.addClassToInstrument(srcClass);
        }

        logger.debug("FL starts gz.run()");
        gz.run();
        logger.debug("FL ends gz.run()");
        Spectra spectra = gz.getSpectra();

        // get test result
        List<TestResult> testResults = spectra.getTestResults();
        logger.info("Total tests executed: {}, total componenets (stmts) obtained: {}", testResults.size(),
                spectra.getNumberOfComponents());

        // init matrix
        matrix = new int[testResults.size()][spectra.getComponents().size() + 1];
        List<String> testList = new ArrayList<>();
        List<String> stmtList = new ArrayList<>();

        for (int index = 0; index < testResults.size(); index++) {
            TestResult tr = testResults.get(index);

            testList.add(tr.getName());

            if (tr.wasSuccessful()) {
                totalPassed++;
                matrix[index][spectra.getComponents().size()] = 2;
            } else {

                matrix[index][spectra.getComponents().size()] = -2;

                totalFailed++;
                failedMethods.add(tr.getName());
            }
        }

        // for each component (suspicious stmt)
        for (int index = 0; index < spectra.getComponents().size(); index++) {
            Component component = spectra.getComponents().get(index);
            Statement stmt = (Statement) component;
            String className = stmt.getClazz().getLabel();
            int lineNo = stmt.getLineNumber();
            BitSet coverage = stmt.getCoverage();

            int execPassed = 0;
            int execFailed = 0;
            List<String> execPassedMethods = new ArrayList<>();
            List<String> execFailedMethods = new ArrayList<>();

            // traverse bitset coverage
            // this loop is based on the nextSetBit() javadoc
            for (int i = coverage.nextSetBit(0); i >= 0; i = coverage.nextSetBit(i + 1)) {
                if (i == Integer.MAX_VALUE) {
                    logger.error("i == Integer.MAX_VALUE now.");
                    break; // or (i+1) would overflow
                }

                // operate on index i here
                TestResult tr = testResults.get(i);
                if (tr.wasSuccessful()) {
                    execPassed++;
                    execPassedMethods.add(tr.getName());
                } else {
                    matrix[i][index] = 1;
                    execFailed++;
                    execFailedMethods.add(tr.getName());
                }
            }

            SuspiciousLocation sl = new SuspiciousLocation(className, lineNo, execPassed, execFailed,
                    totalPassed, totalFailed, execPassedMethods, execFailedMethods);
            suspList.add(sl);
            stmtList.add(String.format("%s:%s", className, lineNo));
        }

        Collections.sort(suspList, new Comparator<SuspiciousLocation>() {
            @Override
            public int compare(SuspiciousLocation o1, SuspiciousLocation o2) {
                // descending order
                return Double.compare(o2.getSuspValue(), o1.getSuspValue());
            }
        });

        int posCnt = 0;
        for (SuspiciousLocation sl : suspList) {
            FileUtil.writeToFile(savePath, sl.toString() + "\n");

            if (sl.getSuspValue() > 0) {
                posCnt++;
            }
        }
        
        FileUtil.writeMatrixFile(matrix, testList, stmtList);
        logger.info("FL ends.");
    }

    public List<String> getFailedMethods() {
        return failedMethods;
    }

    public void setFailedMethods(List<String> failedMethods) {
        this.failedMethods = failedMethods;
    }

    /**
     * @return the matrix
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * @param matrix the matrix to set
     */
    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }
}
