/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javadocparser.ParseException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import apr.apr.repair.utils.FileUtil;
import apr.apr.repair.utils.NodeUtil;

import com.github.javaparser.ast.Node.TreeTraversal;

/**
 * Try to obtain all code blocks given a compilation unit for similar code search
 * @author apr
 * @version Mar 23, 2020
 *
 */
public class CodeBlocks {
	final static Logger logger = LoggerFactory.getLogger(CodeBlocks.class);
	
	List<CodeFragment> codeFrags = new ArrayList<>();
	
	private int fragSize = 10; // at least 10 lines for a code fragment
	private int maxFragSize = 20;
	
	public CodeBlocks(CompilationUnit cu){
		// parse all nodes
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods){
//			codeFrags.add(new CodeFragment(method)); // add the method itself
//			logger.debug(NodeUtil.getPrettyPrintNode(method));
			
//			if(method.toString().contains("protected Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor, double x, double y, PlotOrientatio")){
//				logger.info("");
//			}
			
			codeFrags.addAll(getCodeblocks(method));
		}
//		logger.debug("a breakpoint");
	}

	@Override
	public String toString(){
		String str = "";
		for (CodeFragment cf : codeFrags){
			str += cf.toString() + "\n";
		}
		return str;
	}
	
	/** @Description 
	 * refer to: "SLACC: Simion-based Language Agnostic Code Clones"
	 * @author apr
	 * @version Mar 23, 2020
	 *
	 */
	private List<CodeFragment> getCodeblocks(Node node) {
		List<CodeFragment> cfs = new ArrayList<>();
		
		if (NodeUtil.getLineRange(node) <= maxFragSize){
			cfs.add(new CodeFragment(node)); // add node itself
		}
		
		List<Node> children = node.getChildNodes();
		List<Statement> stmts = new ArrayList<>();
		for(Node child : children){
			if (child instanceof Statement){
				stmts.add((Statement) child);
			}
//			else{
//				logger.info("child: {} (type: {}) is not a statement.", child.toString(), child.getClass());
//			}
		}
		
//		List<Statement> stmts = node.findAll(Statement.class);
//		
//		// exclude the node itself
//		if(node instanceof Statement){
//			int index = NodeUtil.getIndex(stmts, node);
//			stmts.remove(index);
//		}
		
		for (int i = 0; i < stmts.size(); i++){  //  - 1
			CodeFragment cf = new CodeFragment();
			Statement stmti = stmts.get(i);
			for (int j = i; j < stmts.size(); j++){
				Statement stmtj = stmts.get(j);
				cf.addNode(stmtj);
				
				if (cf.getLineRangeSize() > maxFragSize){
					break;
				}
				
				if (cf.getLineRangeSize() >= fragSize){ // && cf.getLineRangeSize() <= maxFragSize
					cfs.add(cf);
				}
				
				if (!stmtj.getChildNodes().isEmpty()){ // && NodeUtil.getLineRange(stmtj) >= fragSize
					cfs.addAll(getCodeblocks(stmtj));
				}
			}
		}
		
		return cfs;
	}
}
