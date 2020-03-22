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
//	private CompilationUnit cuSymbol;
	
//	private List<VariableNode> fieldVariables = new ArrayList<>();
	private List<VariableNode> variables = new ArrayList<>();
	
	
	/**
	 * construct class
	 * @param lineNo
	 * @param clazz
	 * @param directory
	 */
	public NodeFinder(int lineNo, String clazz, String directory){
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
//			TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
//	        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(srcJavaDir));
//			CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
//	        combinedSolver.add(reflectionTypeSolver);
//	        combinedSolver.add(javaParserTypeSolver);
//	        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
//	        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
//	        
//	        cuSymbol = StaticJavaParser.parse(new File(srcPath));
//			List<FieldDeclaration> fds = cuSymbol.findAll(FieldDeclaration.class);
//			System.out.println("Field type: " + fds.get(0).getVariables().get(0).getType()
//					.resolve().asReferenceType().getQualifiedName());
//			System.exit(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		// a test: lineNo -> 154 (stmt) , 153 (}), 41 (import), 64 (field declare), 333 method, 78 class construct, 73 empty
		NodeFinder sf = new NodeFinder(151, "com.google.javascript.jscomp.CoalesceVariableNames", 
				"/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"); 
		// , "/mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/"
		///mnt/benchmarks/repairDir/Kali_Defects4J_Closure_18/src/com/google/javascript/jscomp/CoalesceVariableNames.java
		Node targetNode = sf.findTargetNode();
//		YamlPrinter printer = new YamlPrinter(true);
//		logger.info("format print targetNode: {}", printer.output(targetNode));
		
		PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
//		conf.setIndentSize(" ");
		conf.setPrintComments(false);
		PrettyPrinter prettyPrinter = new PrettyPrinter(conf);
		System.out.println(prettyPrinter.print(targetNode));
		
		Node tokenizedNode = sf.tokenize(targetNode);
		
		
		// output tree structure picture
//		DotPrinter printer2 = new DotPrinter(true);
//		try (FileWriter fileWriter = new FileWriter("ast.dot");
//		    PrintWriter printWriter = new PrintWriter(fileWriter)) {
//		    printWriter.print(printer2.output(targetNode));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		CompilationUnit cu = sf.getCU();
//		cu.getTokenRange().get().forEach(t -> logger.debug(t.getText()));
//		LexicalPreservingPrinter.setup(cu);
//		FileUtil.writeToFile(System.getProperty("user.dir") + "/ast.print",LexicalPreservingPrinter.print(cu) , false);
		
		// http://javaparser.org/inspecting-an-ast/
//		YamlPrinter printer = new YamlPrinter(true);
//		FileUtil.writeToFile(System.getProperty("user.dir") + "/ast.print", printer.output(cu), false);
		
		// get vars
//		sf.getBasicVariables();
//		sf.getAllVariables();
//		sf.printVars();
	}
	
	
	public String getPrettyPrintNode(Node node){
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
	public Node tokenize(Node targetNode) {
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
	
	public void getAllVariables(Map<String, ClassNode> classVarMap){

		Consumer<Node> consumer = n -> {
			// get field declaration variables
			if (n instanceof FieldDeclaration){
				NodeList<VariableDeclarator> vars = ((FieldDeclaration) n).getVariables();
				for (VariableDeclarator var : vars){
					Type type = var.getType();
					String varName = var.getNameAsString();
					getVariable(var, varName, type, variables);
				}
			}
			
			if (n instanceof VariableDeclarator){
				Type type = ((VariableDeclarator) n).getType();
				String name = ((VariableDeclarator) n).getNameAsString();
//				variables.add(getVariable(n, name, type));
				getVariable(n, name, type, variables);
			}
			
			if (n instanceof MethodDeclaration){
				NodeList<Parameter> paras = ((MethodDeclaration) n).getParameters();
				for (Parameter para : paras){
//					if (variables.containsKey(para.getName().asString())) logger.debug("repeated para");
//					variables.put(para.getName().asString(), para.getType().asString());
					Type type = para.getType();
					String name = para.getNameAsString();
//					variables.add(getVariable(para, name, type));
					getVariable(para, name, type, variables);
				}
			}
			
			// e.g., this.compiler, v1.index
			if (n instanceof FieldAccessExpr){
				SimpleName sn = ((FieldAccessExpr) n).getName();
				Expression exp = ((FieldAccessExpr) n).getScope();
				
				// e.g.,
//				target(Type=FieldAccessExpr): 
//              name(Type=SimpleName): 
//                  identifier: "compiler"
//              scope(Type=ThisExpr): 		
				if (exp instanceof ThisExpr){
					String className = getClassName(n);
					String typeName = getTypeName(classVarMap, sn.getIdentifier(), className); //sn.getIdentifier() -> varName
					getVariable(n, "this." + sn.getIdentifier(), typeName, variables);
				}
				
				// debug
//				if (sn.getIdentifier().equals("use")){
//					logger.debug("");
//				}
				
				if (exp instanceof NameExpr){
					String scopeName = ((NameExpr) exp).getNameAsString();
					if (Character.isUpperCase(scopeName.charAt(0))){
						logger.info("scopeName: {} is skipped as it is not a var", scopeName);
					}else{
						// CoalesceVariableNames
						String className = getClassName(n);
						String scopeTypeName = getTypeName(classVarMap, scopeName, className);
						if (scopeTypeName == null){
							// find in current vars
							for (VariableNode var : variables){
								if (var.getVarName().equals(scopeName)){
									scopeTypeName = var.getVarOriType();
									break;
								}
							}
						}
						
						String varTypeName =  classVarMap.get(scopeTypeName).getVarMap().get(sn.getIdentifier());
						
						getVariable(n, scopeName + "." + sn.getIdentifier(), varTypeName, variables);
					}
				}
			}
		};
		
		cu.walk(TreeTraversal.PREORDER, consumer);
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 21, 2020
	 *
	 * @param n
	 * @return
	 */
	private String getClassName(Node n) {
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
	 * @Description 
	 * @author apr
	 * @version Mar 21, 2020
	 *
	 * @param classVarMap
	 * @param varName
	 * @param className
	 * @return
	 */
	private String getTypeName(Map<String, ClassNode> classVarMap, String varName, String className) {
		ClassNode cn;
		
		if (classVarMap.containsKey(className)){
			cn = classVarMap.get(className);
			Map<String, String> varMap = cn.getVarMap();
			if (varMap.containsKey(varName)){
				return varMap.get(varName);
			}else{
				logger.error("The varName: {} in className: {} is not found in classVarMap.", varName, className);
				return null;
			}
		}else{
			logger.error("The className: {} is not found in classVarMap.", className);
			return null;
		}
	}

	private void getVariable(Node n, String name, String typeName, List<VariableNode> variables) {
		VariableNode vn = new VariableNode(n, name, typeName);
		if (!variables.contains(vn)){
			variables.add(vn);
		}else{
			logger.debug("repeated node: {}", vn.getVarName());
		}
	}
	
	/** @Description  get a variable 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @param type
	 * @param variables2
	 */
	private void getVariable(Node n, String name, Type type, List<VariableNode> variables) {
		String typeName = type.asString();
		VariableNode vn = new VariableNode(n, name, typeName);
//		if (type instanceof ReferenceType){
//			vn.setVarRefType(type.resolve().asReferenceType().getQualifiedName());
//		}
		
//		if (vn.toString().contains("Variable: coloringTieBreaker = new Co")){
//			logger.debug("break point here");
//			
//			if (!variables.isEmpty()){
//				for (VariableNode var : variables){
//					boolean equal = var.equals(vn);
//					logger.debug("equal: {}", equal);
//				}
//			}
//			
//			if (!variables.contains(vn)){
//				variables.add(vn);
//			}else{
//				logger.debug("repeated node: {}", vn.getVarName());
//			}
//		}
		
		if (!variables.contains(vn)){
			variables.add(vn);
		}else{
			logger.debug("repeated node: {}", vn.getVarName());
		}
	}

	/** @Description print all collected variabls from the compilation unit.
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 */
	public void printVars() {
		for (VariableNode var : variables){
			logger.info(var.toString());
		}
	}

	/**
	 * @Description get targetNode (by invoking FindNode() method) and check if null 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	public Node findTargetNode(){
		FindNode();
		
		if(targetNode != null){
			logger.debug("targetNode is: {}, its lineNo: {}, className: {}", targetNode.toString(), lineNo, className);
		}else{
			logger.warn("targetNode is null. its lineNo: {}, className: {}", lineNo, className);
		}
		
		return targetNode;
	}
	
	public Node getTargetNode(){
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
				if (getStartLineNo(n) == lineNo || lineNo == getEndLineNo(n)){
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
	
	public CompilationUnit getCU(){
		return cu;
	}

	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param fragSize
	 * @return
	 */
	public List<Node> getCodeFragment(int fragSize, Node curNode) {
		List<Node> nodes = new ArrayList<>();
		
		nodes.add(curNode);
		
//		int targetIndex = 0; 
		
		int currentSize = getLineRange(curNode);
		int cnt = 0; // to avoid infinite loop
		while ( currentSize <= fragSize ){
			cnt ++;
			if (cnt >= 10000){
				logger.error("stop after 10000 times while loop.");
				break;
			}
			
			Node parent = curNode.getParentNode().get();
			if (parent == null){
				logger.warn("parent is null, exit the loop");
				break;
			}
			
			if (parent instanceof ClassOrInterfaceDeclaration){ // parent instanceof MethodDeclaration || 
				logger.warn("parent is {}, exit the loop", parent.getClass());
				break;
			}
			
			if (getLineRange(parent) <= fragSize){
				nodes.remove(0);
				nodes.add(parent);
				
				currentSize = getLineRange(parent);
				curNode = parent;
				continue;
			}
			
			List<Node> siblings = parent.getChildNodes();
			int index = siblings.indexOf(curNode);  // debug: when two same nodes in a parent
			int preCnt = 1;
//			int preStop = false;
			int nextCnt = 1;
			boolean traverseAllPreFlag = false;
			boolean traverseAllNextFlag = false;
			
			boolean stopPre = false;
			boolean stopNext = false;
			while (currentSize <= fragSize){
				if (index - preCnt >= 0 && !stopPre){ // search previous node & try to add
					Node preSib = siblings.get(index - preCnt);
					preCnt ++;
					
					int preSibSize = getLineRange(preSib);
					if (preSibSize + currentSize <= fragSize){
						currentSize += preSibSize;
						nodes.add(0, preSib);
					}else{
						stopPre = true;
					}
				}else{
					traverseAllPreFlag = true;
				}
				
				if (index + nextCnt < siblings.size() && !stopNext){ // search next node & try to add
					Node nextSib = siblings.get(index + nextCnt);
					nextCnt ++;
					
					int nextSibSize = getLineRange(nextSib);
					if (nextSibSize + currentSize <= fragSize){
						currentSize += nextSibSize;
						nodes.add(nodes.size(), nextSib);
					}else{
						stopNext = true;
					}
				}else{
					traverseAllNextFlag = true;
				}
				
				if (traverseAllPreFlag && traverseAllNextFlag) {
					break;
				}
				
				if (stopPre && stopNext){
					break;
				}
			}
			
			if (stopPre || stopNext){
				break;
			}
			
			curNode = parent;
//			for (Node sib : siblings){
//				if (sib.getTokenRange().get() != targetNode.getTokenRange().get()){ // debug if correct
//					List
//				}
//			}
		}
		
		return nodes;
	}

	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param preSib
	 * @return
	 */
	private int getLineRange(Node node) {
		return getEndLineNo(node) - getStartLineNo(node) + 1;
	}
}
