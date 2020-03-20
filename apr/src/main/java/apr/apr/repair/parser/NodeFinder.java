/**
 * 
 */
package apr.apr.repair.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javadocparser.ParseException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
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
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import apr.apr.repair.utils.FileUtil;

import com.github.javaparser.ast.Node.TreeTraversal;



/**
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class NodeFinder {
	final static Logger logger = LoggerFactory.getLogger(NodeFinder.class);
	
	private int lineNo;
	private String className;
	private Node targetNode;
	private CompilationUnit cu;
	private CompilationUnit cuSymbol;
	
	private List<VariableNode> variables = new ArrayList<>();
	
	public static void main(String[] args){
		// a test: lineNo -> 154 (stmt) , 153 (}), 41 (import), 64 (field declare), 333 method, 78 class construct 
		NodeFinder sf = new NodeFinder(78, "com.google.javascript.jscomp.CoalesceVariableNames", 
				"/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/", "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"); 
		///mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/com/google/javascript/jscomp/CoalesceVariableNames.java
//		sf.getTargetNode();
		
		// get vars
		sf.getBasicVariables();
		sf.getAllVariables();
		sf.printVars();
	}
	
	/**
	 * construct class
	 * @param lineNo
	 * @param clazz
	 * @param directory
	 */
	public NodeFinder(int lineNo, String clazz, String directory, String srcJavaDir){
		this.lineNo = lineNo;
		this.className = clazz;
		
		// get src path
		if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
			directory += "/";
		}
		String srcPath = directory + clazz.replace(".", "/") + ".java";
		
		// get compilation unit
		try {
			cu = StaticJavaParser.parse(new File(srcPath));
			
			// get cu with symbol solver
			TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
	        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(srcJavaDir));
			CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
	        combinedSolver.add(reflectionTypeSolver);
	        combinedSolver.add(javaParserTypeSolver);
	        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
	        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
	        
	        cuSymbol = StaticJavaParser.parse(new File(srcPath));
			List<FieldDeclaration> fds = cuSymbol.findAll(FieldDeclaration.class);
//	        System.out.println("Field type: " + fds.get(1).getVariables().get(0).getType()
//	                .resolve().asReferenceType().getQualifiedName());
			System.out.println("Field type: " + fds.get(0).getVariables().get(0).getType()
					.resolve().asReferenceType().getQualifiedName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void getBasicVariables(){
		Consumer<Node> consumer = n -> {
			if (n instanceof VariableDeclarator){
				Type type = ((VariableDeclarator) n).getType();
				String typeName = type.asString();
				VariableNode vn = new VariableNode(n, ((VariableDeclarator) n).getName().asString(), typeName);
				if (type instanceof ReferenceType){
					vn.setVarRefType(type.resolve().asReferenceType().getQualifiedName());
//					typeName = type.resolve().asReferenceType().getQualifiedName();
				}
//				variables.put(((VariableDeclarator) n).getName().asString(), typeName);
				variables.add(vn);
			}
			
			if (n instanceof MethodDeclaration){
				NodeList<Parameter> paras = ((MethodDeclaration) n).getParameters();
				for (Parameter para : paras){
//					if (variables.containsKey(para.getName().asString())) logger.debug("repeated para");
//					variables.put(para.getName().asString(), para.getType().asString());
					Type type = para.getType();
					String typeName = "";
					if (type instanceof ReferenceType){
//						try{
						typeName = type.resolve().asReferenceType().getQualifiedName();
//						}catch (Exception e){ //UnsupportedOperationException ParseException
////							e.printStackTrace();
//							logger.info("ParseException----------");
//							if (e instanceof ParseException){
//								logger.info("-------------ParseException----------");
//							}
//						}
					}else{
						typeName = type.asString();
					}
					variables.put(para.getName().asString(), typeName);
					
				}
			}
		};
		
		cuSymbol.walk(TreeTraversal.PREORDER, consumer);
	}
	
	public void getAllVariables(){
		Consumer<Node> consumer = n -> {
//			if (n instanceof FieldAccessExpr){
//				NodeList<Type> types = ((FieldAccessExpr) n).getTypeArguments().get();
//				for (Type type : types){
//					logger.debug("type: {}", type.resolve().asReferenceType().getQualifiedName());
//				}
//				((FieldAccessExpr) n).getName()
//			}
			
//			if (n instanceof FieldAccessExpr){
//				n.getL
//			}
			
			if (n instanceof MethodDeclaration){
				NodeList<Parameter> paras = ((MethodDeclaration) n).getParameters();
				for (Parameter para : paras){
//					if (variables.containsKey(para.getName().asString())) logger.debug("repeated para");
//					variables.put(para.getName().asString(), para.getType().asString());
					Type type = para.getType();
					String typeName = "";
					if (type instanceof ReferenceType){
//						try{
						typeName = type.resolve().asReferenceType().getQualifiedName();
//						}catch (Exception e){ //UnsupportedOperationException ParseException
////							e.printStackTrace();
//							logger.info("ParseException----------");
//							if (e instanceof ParseException){
//								logger.info("-------------ParseException----------");
//							}
//						}
					}else{
						typeName = type.asString();
					}
					
					variables.put(para.getName().asString(), typeName);
					
				}
			}
		};
		
		cuSymbol.walk(TreeTraversal.PREORDER, consumer);
	}
	
	/** @Description print all collected variabls from the compilation unit.
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 */
	public void printVars() {
		for (Map.Entry<String, String> entry : variables.entrySet()){
			logger.info("variable: {}, type: {}", entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @Description get targetNode (by invoking FindNode() method) and check if null 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	public Node getTargetNode(){
		FindNode();
		
		if(targetNode != null){
			logger.debug(targetNode.toString());
		}else{
			logger.warn("targetNode is null. its lineNo: {}, className: {}", lineNo, className);
		}
		
		return targetNode;
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
			* ConstructorDeclaration
			* A constructor declaration: class X { X() { } } where X(){} is the constructor declaration.
			*/
			if (n instanceof FieldDeclaration || n instanceof InitializerDeclaration || n instanceof MethodDeclaration
					|| n instanceof TypeDeclaration  || n instanceof ConstructorDeclaration){
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
