package apr.apr.repair.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import apr.apr.repair.utils.FileUtil;

public class Main {
	public static void main(String[] args){
		setParameters(args);
	}
	
	/*
	 * receive parameters
	 */
	private static void setParameters(String[] args) {		
//		Map<String, String> parameters = new HashMap<>();
		
        Option opt1 = new Option("srcJavaDir","srcJavaDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/src");
        opt1.setRequired(true);
        Option opt2 = new Option("binJavaDir","binJavaDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/build/classes/main/ ");
        opt2.setRequired(true);   
        Option opt3 = new Option("binTestDir","binTestDir",true,"e.g., /mnt/benchmarks/repairDir/Kali_Defects4J_Mockito_10/build/classes/test/ ");
        opt3.setRequired(true);
        Option opt4 = new Option("dependences","dependences",true,"all dependencies");
        opt4.setRequired(true);
        Option opt5 = new Option("buggylocDir","buggylocDir",true,"e.g., /mnt/benchmarks/buggylocs/Defects4J/Defects4J_Mockito_10/");
        opt4.setRequired(true);
        Option opt6 = new Option("externalProjPath","externalProjPath",true,"Path to run junit tests. e.g., /home/apr/apr_tools/tbar-ori/TBar-dale/externel/target/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        opt4.setRequired(true);
        Option opt7 = new Option("jvmPath","jvmPath",true,"java path to run junit tests (e.g., /home/apr/env/jdk1.7.0_80/jre/bin/java)");
        opt4.setRequired(true);
        Option opt8 = new Option("failedTestsStr","failedTestsStr",true,"e.g., com.google.javascript.jscomp.CollapseVariableDeclarationsTest");
        opt4.setRequired(true);

        Options options = new Options();
        options.addOption(opt1);
        options.addOption(opt2);
        options.addOption(opt3);
        options.addOption(opt4);
        options.addOption(opt5);
        options.addOption(opt6);
        options.addOption(opt7);
        options.addOption(opt8);

        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        try {
            cli = cliParser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            helpFormatter.printHelp(">>>>>> test cli options", options);
            e.printStackTrace();
        } 
        
        try {
	        if (cli.hasOption("srcJavaDir")){
	        	String srcJavaDir = cli.getOptionValue("srcJavaDir");
	        	
	        	FileUtil.srcJavaDir = new File(srcJavaDir).getCanonicalPath();
	//        	parameters.put("srcJavaDir", cli.getOptionValue("srcJavaDir"));
	        }
	        if(cli.hasOption("binJavaDir")){
	        	String binJavaDir = cli.getOptionValue("binJavaDir");
	        	
	        	FileUtil.binJavaDir = new File(binJavaDir).getCanonicalPath();
	//        	parameters.put("binJavaDir", cli.getOptionValue("binJavaDir"));
	        }
	        if(cli.hasOption("binTestDir")){
	        	String binTestDir = cli.getOptionValue("binTestDir");
	        	
	        	FileUtil.binTestDir = new File(binTestDir).getCanonicalPath();
	        	
	//        	parameters.put("binTestDir", cli.getOptionValue("binTestDir"));
	        }
	        if(cli.hasOption("dependences")){
	        	FileUtil.dependencies = cli.getOptionValue("dependences");
	        	for(String dep : FileUtil.dependencies.split(":")){
	        		if(! dep.isEmpty() && new File(dep).exists()){
	        			
	        				String path = new File(dep).getCanonicalPath();
	        				if (FileUtil.depsList.contains(path)) continue;
	        				
							FileUtil.depsList.add(path);
						
	        		}else{
	        			System.out.format("dependency: %s is empty or does not exist.", dep);
	        		}
	        	}
	//        	parameters.put("dependences", cli.getOptionValue("dependences"));
	        }
	        if(cli.hasOption("buggylocDir")){
	        	String buggylocDir = cli.getOptionValue("buggylocDir");
	        	FileUtil.buggylocDir = buggylocDir;
	        	FileUtil.flPath = new File(buggylocDir).getAbsolutePath() + "/FL.txt";
	        	FileUtil.buggylocPath = new File(buggylocDir).getAbsolutePath() + "/buggyloc.txt";
	        	
	        	// to be written
	        	FileUtil.flLogPath = new File(buggylocDir).getAbsolutePath() + "/fl_log_" + FileUtil.toolName + ".txt";
	        	FileUtil.searchLogPath = new File(buggylocDir).getAbsolutePath() + "/search_log_" + FileUtil.toolName + ".txt";
	        	FileUtil.changedFLPath = new File(buggylocDir).getAbsolutePath() + "/changedFL_" + FileUtil.toolName + ".txt";
	        	FileUtil.positiveTestsPath = new File(buggylocDir).getAbsolutePath() + "/allPosTests_" + FileUtil.toolName + ".txt";
	        	FileUtil.filteredPositiveTestsPath = new File(buggylocDir).getAbsolutePath() + "/filteredPosTests_" + FileUtil.toolName + ".txt";
	        	
	        	FileUtil.writeToFile(FileUtil.flLogPath, "", false);
	        	FileUtil.writeToFile(FileUtil.searchLogPath, "", false); // init
	        	
	//        	parameters.put("buggylocDir", cli.getOptionValue("buggylocDir"));
	        }
	        if(cli.hasOption("externalProjPath")){
	        	FileUtil.externalProjPath = cli.getOptionValue("externalProjPath");
	        }
	        if(cli.hasOption("jvmPath")){
	        	try {
					FileUtil.jvmPath = new File(cli.getOptionValue("jvmPath")).getCanonicalPath() + "/java";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if(cli.hasOption("failedTestsStr")){
	        	FileUtil.failedTestsStr = cli.getOptionValue("failedTestsStr");
	        	FileUtil.oriFailedTests = Arrays.asList(FileUtil.failedTestsStr.split(":"));
	        }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // make sure that src/ test-classes/ src-classes/ are contained
     	if( ! FileUtil.depsList.contains(FileUtil.srcJavaDir)){
     		FileUtil.depsList.add(FileUtil.srcJavaDir);
     	}
     	if( ! FileUtil.depsList.contains(FileUtil.binJavaDir)){
     		FileUtil.depsList.add(FileUtil.binJavaDir);
     	}
     	if( ! FileUtil.depsList.contains(FileUtil.binTestDir)){
     		FileUtil.depsList.add(FileUtil.binTestDir);
     	}
        
//		return parameters;
    }
}
