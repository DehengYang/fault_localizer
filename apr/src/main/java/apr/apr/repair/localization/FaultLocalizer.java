package apr.apr.repair.localization;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.gzoltar.core.GZoltar;

import apr.apr.repair.utils.FileUtil;

public class FaultLocalizer  {
	private static String workDir = System.getProperty("user.dir");

	public FaultLocalizer(String wD) throws FileNotFoundException, IOException {
		GZoltar gz = new GZoltar(workDir);
		
		// set classpath
		gz.setClassPaths(FileUtil.depsList);
		
		// test classes to execute
		
		
	}
	
}
