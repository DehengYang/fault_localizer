/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.WhileStmt;


/** Refer to: JavaParser document
 * Parse the java source file.
 * @author apr
 * @version Mar 19, 2020
 *
 */
public class FileParser {
	final static Logger logger = LoggerFactory.getLogger(FileParser.class);

	private ParseResult<CompilationUnit> result;
	private CompilationUnit cu;

	public FileParser(String clazz, String directory){
		// get src path
		if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
			directory += "/";
		}
		String srcPath = directory + clazz.replace(".", "/") + ".java";
		
		// get cu
//		cu = StaticJavaParser.parse(srcPath);
		try {
			JavaParser jp = new JavaParser();
			result = jp.parse(new File(srcPath));
			cu = result.getResult().get();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// operate on cu
		operateCU(cu);
	}

	/** @Description 
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @param cu2
	 */
	private void operateCU(CompilationUnit cu) {
		logger.debug("cu type: {}, {}", cu.getPrimaryType(), cu.getPrimaryTypeName() );
		
		WhileStmt ws = cu.findFirst(WhileStmt.class).get();
		logger.debug("while stmt: {}", ws.findAll(SimpleName.class));
		
	}
}
