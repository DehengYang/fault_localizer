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
			List<FieldDeclaration> fds = cu2.findAll(FieldDeclaration.class);
//	        System.out.println("Field type: " + fds.get(1).getVariables().get(0).getType()
//	                .resolve().asReferenceType().getQualifiedName());
			System.out.println("Field type: " + fds.get(1).getVariables().get(0).getTypeAsString()  );
			
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
				if (n instanceof ForStmt){ //EnumDeclaration, FieldDeclaration
					n.findAll(VariableDeclarationExpr.class).forEach( expr -> {
						NodeList<VariableDeclarator> vars = expr.getVariables();
						for ( VariableDeclarator var : vars){
							logger.debug("variable: {}, {}, {}", var.getName(), var.getType(), var.getParentNode());
						}
//						vars.forEach( var -> logger.debug("variable: {}, {}, {}", var, var.getParentNode()););
//						logger.debug("variable: {}, {}, {}", var.getVariables(), var.getParentNode());
					});
					
//					((FieldDeclaration) n).getVariables().forEach( var -> {
//						logger.debug("variable: {}, {}, {}", var.getName(), var.getType(), var.getParentNode());
//					});
//					logger.debug("variable: {}", ((FieldDeclaration) n).getVariables().toString());
				}
			};
			node.walk(TreeTraversal.PREORDER, consumer);
			
			break;
		}
		
		
	}
}
