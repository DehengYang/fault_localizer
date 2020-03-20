/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

/**
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class StmtFinder {
	final static Logger logger = LoggerFactory.getLogger(StmtFinder.class);
	
	private int lineNo;
	private Node targetNode;
	private CompilationUnit cu;
	
	public static void main(String[] args){
		// a test: lineNo -> 154 (stmt) , 153 (}), 41 (import), 64 (field declare), 333 method, 
		StmtFinder sf = new StmtFinder(64, "com.google.javascript.jscomp.CoalesceVariableNames", "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"); 
		///mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/com/google/javascript/jscomp/CoalesceVariableNames.java
	}
	
	public StmtFinder(int lineNo, String clazz, String directory){
		this.lineNo = lineNo;
		
		// get src path
		if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
			directory += "/";
		}
		String srcPath = directory + clazz.replace(".", "/") + ".java";
		
		// get compilation unit
		try {
			cu = StaticJavaParser.parse(new File(srcPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		FindNode();
		
		if(targetNode != null){
			logger.debug(targetNode.toString());
		}else{
			logger.warn("targetNode is null. its lineNo: {}, className: {}", lineNo, clazz);
		}
	}
	
	/**
	 * @Description find a node. 
	 * 
	 * refer to: How to do a complete walkover of the generated AST? #2509 -> https://github.com/javaparser/javaparser/issues/2509
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 */
	public void FindNode(){
		Consumer<Node> consumer = n -> {
//			logger.debug("node: {}", n.getClass());
			
			if (n instanceof Statement){ //EnumDeclaration, FieldDeclaration
				if (getStartLineNo(n) <= lineNo && lineNo <= getEndLineNo(n)){
					targetNode = n;
					logger.debug("stmt info: {}, {}, {}", n.getClass(), n.getBegin(), n.getEnd() );
//					return;
				}
			}
			
			/* 
			* FieldDeclaration
			* The declaration of a field in a class. "private static int a=15*15;" in this example: class X { private static int a=15*15; }
			* InitializerDeclaration
			* A (possibly static) initializer body. "static { a=3; }" in this example: class X { static { a=3; } }
			* MethodDeclaration
			* A method declaration. "public int abc() {return 1;}" in this example: class X { public int abc() {return 1;} }
			* TypeDeclaration
			* A base class for all types of type declarations.
			*/
			if (n instanceof FieldDeclaration || n instanceof InitializerDeclaration || n instanceof MethodDeclaration
					|| n instanceof TypeDeclaration ){
				if (getStartLineNo(n) <= lineNo && lineNo <= getEndLineNo(n)){
					targetNode = n;
					logger.debug("declaration info: {}, {}, {}", n.getClass(), n.getBegin(), n.getEnd() );
//					return;
				}
			}
		};
		
		cu.walk(TreeTraversal.PREORDER, consumer);
	}
	
	/**
	 * @Description get start line number of the node 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	public int getStartLineNo(Node node){
		Range range = node.getRange().get();
		return range.begin.line;
	}
	
	
	public int getEndLineNo(Node node){
		Range range = node.getRange().get();
		return range.end.line;
	}
}
