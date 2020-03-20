/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.printer.YamlPrinter;
//import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import apr.apr.repair.utils.FileUtil;


/** Refer to: JavaParser document
 * Parse the java source file.
 * @author apr
 * @version Mar 19, 2020
 *
 */
public class AttemptFileParser {
	final static Logger logger = LoggerFactory.getLogger(AttemptFileParser.class);

	private ParseResult<CompilationUnit> result;
	private CompilationUnit cu;

	public AttemptFileParser(String clazz, String directory){
		// get src path
		if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
			directory += "/";
		}
		String srcPath = directory + clazz.replace(".", "/") + ".java";
		
		// set symbol solver
//		TypeSolver typeSolver = new CombinedTypeSolver();
//		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
//		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
		TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(FileUtil.srcJavaDir));
//        reflectionTypeSolver.setParent(reflectionTypeSolver);

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(reflectionTypeSolver);
        combinedSolver.add(javaParserTypeSolver);
        
        for (String dep : FileUtil.depsList){
        	try {
        		if (dep.endsWith(".jar")){
        			JarTypeSolver js = new JarTypeSolver(new File(dep));
        			combinedSolver.add(js);
//        			combinedSolver.add(JarTypeSolver.getJarTypeSolver(dep));
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        

        // details for symbolsolver:
        // refer to: How to find the begin line and paramters's full name in some method simply? #2053 -> https://github.com/javaparser/javaparser/issues/2053 
        // About the Symbol Solver -> https://github.com/javaparser/javaparser/wiki/About-the-Symbol-Solver
        
        
        
        // error will occur: com.github.javadocparser.ParseException: Encountered " <ENTER_JAVADOC_COMMENT> 
        // refer to: https://github.com/javaparser/javaparser/issues/174
        // https://github.com/javaparser/javaparser/issues/112
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser
                .getConfiguration()
                .setSymbolResolver(symbolSolver);
		
		// get cu
//		cu = StaticJavaParser.parse(srcPath);
		try {
//			JavaParser jp = new JavaParser();
//			result = jp.parse(new File(srcPath));
//			cu = result.getResult().get();
			
			cu = StaticJavaParser.parse(new File(srcPath));
			
			CompilationUnit cu2 = StaticJavaParser.parse(new File(srcPath));
//			List<AssignExpr> aes = cu2.findAll(AssignExpr.class);
//			for(AssignExpr ae : aes){
//				 ResolvedType resolvedType = ae.calculateResolvedType();
//		         System.out.println(ae.toString() + " is a: " + resolvedType);
//			}
//			FieldDeclaration fieldDeclaration = Navigator.demandNodeOfGivenClass(cu2, FieldDeclaration.class);
//			List<FieldDeclaration> fds = cu2.findAll(FieldDeclaration.class);
//	        System.out.println("Field type: " + fds.get(1).getVariables().get(0).getType()
//	                .resolve().asReferenceType().getQualifiedName());
//			System.out.println("Field type: " + fds.get(0).getVariables().get(0).getType()
//					.resolve().asReferenceType().getQualifiedName());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// operate on cu
		operateCU(cu);
	}

	/**
	 * @Description get formatted output of cu. 
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @param cu
	 */
	private void printCU(CompilationUnit cu){
		// refer to: Inspecting an AST -> http://javaparser.org/inspecting-an-ast/
		// Now comes the inspection code:
		YamlPrinter printer = new YamlPrinter(true);
		logger.debug(printer.output(cu));
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @param cu2
	 */
	private void operateCU(CompilationUnit cu) {		
//		logger.debug("cu type: {}, {}", cu.getPrimaryType(), cu.getPrimaryTypeName() );
		
		WhileStmt ws = cu.findFirst(WhileStmt.class).get();
		Range range = ws.getRange().get();
		logger.debug("while stmt: {}, vars: {}, range: {}, {}", ws.toString(), ws.findAll(SimpleName.class), range.begin, range.end);
		
		List<Node> nodes = cu.getChildNodes();
		for (Node node : nodes){
			node = nodes.get(nodes.size() - 1);
			
//			logger.debug("child node: {}, {}, {}\n\n", node.toString(), node.getRange(), node.getClass());
			
			Consumer<Node> consumer = n -> {
//				logger.debug("node traverse: {}, class: {}, node range: {}", n.toString().trim(), n.getClass().getTypeName(), n.getRange().toString());
//				if (n instanceof ForStmt){ //EnumDeclaration, FieldDeclaration
//					n.findAll(VariableDeclarationExpr.class).forEach( expr -> {
//						NodeList<VariableDeclarator> vars = expr.getVariables();
//						for ( VariableDeclarator var : vars){
//							logger.debug("variable: {}, {}, {}", var.getName(), var.getType(), var.getParentNode());
//						}
//					});
//				}
				
				
//				n.findAll(VariableDeclarationExpr.class).forEach( expr -> {
//					NodeList<VariableDeclarator> vars = expr.getVariables();
//					for ( VariableDeclarator var : vars){
//						logger.debug("variable: {}, {}, {}", var.getName(), var.getType(), var.getParentNode());
//					}
//				});
//				
				if (n instanceof ExpressionStmt){ //EnumDeclaration, FieldDeclaration
					
					if (n.toString().contains("this.callback1 = callback1;")){
						for(Node child : n.getChildNodes()){
							logger.debug("child: {}", child.getClass());
						}
					}
					
					n.findAll(VariableDeclarationExpr.class).forEach( expr -> {
						NodeList<VariableDeclarator> vars = expr.getVariables();
						for ( VariableDeclarator var : vars){
							logger.debug("variable: {}, {}, {}", var.getName(), var.getType().resolve().asReferenceType().getQualifiedName(), var.getParentNode());
						}
					});
				}
			};
			node.walk(TreeTraversal.PREORDER, consumer);
			
			break;
		}
		
		
	}
}
