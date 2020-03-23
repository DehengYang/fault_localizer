/**
 * 
 */
package apr.apr.repair.utils;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

/**
 * @author apr
 * @version Mar 23, 2020
 *
 */
public class NodeUtil {
	final static Logger logger = LoggerFactory.getLogger(NodeUtil.class);
	
	public static String getPrettyPrintNode(Node node){
		PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
//		conf.setIndentSize(" ");
		conf.setPrintComments(false);
		PrettyPrinter prettyPrinter = new PrettyPrinter(conf);
		return prettyPrinter.print(node);
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * e.g., 
	 * if (colorings.isEmpty() || !n.isName() || parent.isFunction()) {
     *     return;
	 * }
	 * -> 
	 * 
	 * if (var.id() || !n.isName() || parent.isFunction()) {
	 * 
	 *
	 * @param targetNode2
	 * @return
	 */
	public static Node tokenize(Node targetNode) {
		Node node = targetNode.clone();
		// identifiers, literals, and variable types
		// id: ClassOrInterfaceType, varName, methodName
		// lit: all literalExpr
//		node.findAll(ClassOrInterfaceType.class);
//		node.findAll(VariableDeclarator.class);
//		node.findAll(FieldDeclaration.class);
//		node.findAll(MethodDeclaration.class);
//		node.findAll(FieldAccessExpr.class);
//		node.findAll(MethodCallExpr.class);
		
		// a simple approach
		Consumer<Node> consumer = n -> {
			if (n instanceof SimpleName){
				((SimpleName) n).setIdentifier("id");
			}
			
			if (n instanceof ClassOrInterfaceType){
				((ClassOrInterfaceType) n).setName("varType");
			}
			
			if (n instanceof LiteralExpr){
				n.replace(new StringLiteralExpr("literal"));
			}
		};
		node.walk(consumer);
		
		logger.info(getPrettyPrintNode(node));
		
		
		return node;
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 21, 2020
	 *
	 * @param n
	 * @return
	 */
	private static String getClassName(Node n) {
		int cnt = 0; // set a limit to avoid infinite loop
		Node parent = n.getParentNode().get();
		while(true){
			cnt ++;
			if (cnt >= 100000) return null;
			
			if (parent instanceof ClassOrInterfaceDeclaration){
				String className = ((ClassOrInterfaceDeclaration) parent).getName().asString();
				return className;
			}else{
				parent = parent.getParentNode().get();
			}
		}
	}
	
	/**
	 * @Description get start line number of the node 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	public static int getStartLineNo(Node node){
		Range range = node.getRange().get();
		return range.begin.line;
	}
	
	
	public static int getEndLineNo(Node node){
		Range range = node.getRange().get();
		return range.end.line;
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param preSib
	 * @return
	 */
	public static int getLineRange(Node node) {
		return getEndLineNo(node) - getStartLineNo(node) + 1;
	}
	
	
}
