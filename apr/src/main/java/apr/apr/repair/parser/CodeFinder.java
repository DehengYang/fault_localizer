/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import apr.apr.repair.utils.FileUtil;

/**
 * @author apr
 * @version Mar 18, 2020
 *
 */
public class CodeFinder {
	
	
	
	/** @Description 
	 * @author apr
	 * @version Mar 18, 2020
	 *
	 * @param srcClasses
	 */
	public void parse(Set<String> srcClasses, String directory) {
		if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
			directory += "/";
		}
		for(String srcClass : srcClasses){
			String srcPath = directory + srcClass.replace(".", "/") + ".java";
			
			// refer to: https://www.programcreek.com/java-api-examples/?api=org.eclipse.jdt.core.dom.ASTParser
			// 
			ASTParser parser = ASTParser.newParser(AST.JLS8); // according to the javadoc
			
			Map<String, String> options = JavaCore.getOptions();
			//TODO: change compiliance level?
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			
			parser.setCompilerOptions(options);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			// refer to: https://bbs.csdn.net/topics/390691501?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task
			parser.setEnvironment(FileUtil.depsList.toArray(new String[FileUtil.depsList.size()]), null, null, true);
			
			String source = null;
			try {
				source = FileUtils.readFileToString(new File(srcPath), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			parser.setSource(source.toCharArray());
			
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			
			System.out.println(srcPath); 
			System.out.println(cu);  
			
			cu.accept(new ASTVisitor() {
				
				public boolean visit(PackageDeclaration _package){
					String packageName = _package.getName().getFullyQualifiedName();
					return false;
				}
			});
		}
		
	}
}
