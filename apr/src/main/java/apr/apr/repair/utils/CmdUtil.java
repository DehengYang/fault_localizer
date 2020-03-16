package apr.apr.repair.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CmdUtil {
	public static String runCmd(String cmd) {
		String output = "";
		
		try{
			String[] commands = {"bash", "-c", cmd};
			Process proc = Runtime.getRuntime().exec(commands);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			
			// read output
			String line = null;
			while ((line = stdInput.readLine()) != null){
				output += line + "\n";
			}
			
			// read error if exists
			String error = "";
			while ((line = stdError.readLine()) != null){
				error += line + "\n";
			}
			if(!error.equals("")){
				System.err.println(String.format("Error/Warning occurs:\n %s\n", error)); // .substring(0, 300): avoid printing too long cmd string.
			}
		}catch (Exception err){
			err.printStackTrace();
		}
		
		return output;
	}
}