/**
 * 
 */
package apr.apr.repair.localization;

import java.io.File;

import apr.apr.repair.utils.CmdUtil;
import apr.apr.repair.utils.FileUtil;

/**
 * support GZoltar 1.7.3
 * @author apr
 * @version Apr 1, 2020
 *
 */
public class FaultLocalizer2 {
	// parameters we need
	private String data_dir; //to save fl results
	private String bug_dir;
	private String test_classpath;
	private String test_classes_dir;
	private String src_classes_dir;
	private String src_classes_file;
	private String all_tests_file;
	private String junit_jar;
	
	// test:
	// 1) other benchmark bugs
	
	public FaultLocalizer2(){
		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/FL"; // direcory
		bug_dir = FileUtil.bugDir;
		test_classpath = FileUtil.dependencies;
		test_classes_dir = FileUtil.binTestDir;
		src_classes_dir = FileUtil.binJavaDir;
		src_classes_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/srcClasses.txt";
		all_tests_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/testClasses.txt";
		junit_jar = FileUtil.junitJar;
	}
	
	/**
	 * @Description get fl results 
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void localize(){
		String cmd = FileUtil.gzoltarSh + " " + data_dir + " " + bug_dir + " " + test_classpath + " " + test_classes_dir + " "
				+ src_classes_dir + " " + src_classes_file + " " + all_tests_file + " " + junit_jar;
		String output = CmdUtil.runCmd(cmd);
	}
	
	/**
	 * @Description get changed fl.
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void logFL(){
		
	}
	

	public String getData_dir() {
		return data_dir;
	}

	public void setData_dir(String data_dir) {
		this.data_dir = data_dir;
	}


	public String getBug_dir() {
		return bug_dir;
	}


	public void setBug_dir(String bug_dir) {
		this.bug_dir = bug_dir;
	}


	public String getTest_classpath() {
		return test_classpath;
	}


	public void setTest_classpath(String test_classpath) {
		this.test_classpath = test_classpath;
	}


	public String getTest_classes_dir() {
		return test_classes_dir;
	}


	public void setTest_classes_dir(String test_classes_dir) {
		this.test_classes_dir = test_classes_dir;
	}


	public String getSrc_classes_dir() {
		return src_classes_dir;
	}


	public void setSrc_classes_dir(String src_classes_dir) {
		this.src_classes_dir = src_classes_dir;
	}


	public String getSrc_classes_file() {
		return src_classes_file;
	}


	public void setSrc_classes_file(String src_classes_file) {
		this.src_classes_file = src_classes_file;
	}


	public String getAll_tests_file() {
		return all_tests_file;
	}


	public void setAll_tests_file(String all_tests_file) {
		this.all_tests_file = all_tests_file;
	}


	public String getJunit_jar() {
		return junit_jar;
	}


	public void setJunit_jar(String junit_jar) {
		this.junit_jar = junit_jar;
	}
	
}
