/**
 * 
 */
package apr.apr.repair.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javadocparser.ParseException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.Node.TreeTraversal;
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
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * @author apr
 * @version Mar 21, 2020
 *
 */
public class ClassVarParser {
	final static Logger logger = LoggerFactory.getLogger(ClassVarParser.class);
	
	private List<String> srcClasses;
	
	// classname -> ClassNode
	private Map<String, ClassNode> classVarMap = new HashMap<>();
	
	public ClassVarParser(List<String> srcClasses, String directory){
		this.srcClasses = srcClasses;
		try{
			for (String srcClass : srcClasses){
				// get src path
				if (! directory.endsWith("/")){ // make sure the src path (as below) is valid.
					directory += "/";
				}
				String srcPath = directory + srcClass.replace(".", "/") + ".java";
				CompilationUnit cu = StaticJavaParser.parse(new File(srcPath));
				
				logger.debug("current parsed path: {}", srcPath);
				
//				Map<String, String> varMap 
//				Consumer<Node> fdConsumer = n -> {
//					if (n instanceof FieldDeclaration){
//						NodeList<VariableDeclarator> vars = ((FieldDeclaration) n).getVariables();
//						for (VariableDeclarator var : vars){
//							Type type = var.getType();
//							String typeName = type.asString();
//							String name = var.getNameAsString();
//							
//						}
//					}
//				};
				
				Consumer<Node> classConsumer = n -> {
					if (n instanceof ClassOrInterfaceDeclaration){
						String className = ((ClassOrInterfaceDeclaration) n).getName().asString();
						Map<String, String> varMap = new HashMap<>();
						List<FieldDeclaration> fds = n.findAll(FieldDeclaration.class);
						for (FieldDeclaration fd : fds){
							NodeList<VariableDeclarator> vars = ((FieldDeclaration) fd).getVariables();
							for (VariableDeclarator var : vars){
								Type type = var.getType();
								String varType = type.asString();
								String varName = var.getNameAsString();
								varMap.put(varName, varType);
							}
						}
						classVarMap.put(className, new ClassNode(className, srcClass, varMap));
					}
				};
				
				cu.walk(TreeTraversal.PREORDER, classConsumer);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<String> getSrcClasses() {
		return srcClasses;
	}

	public void setSrcClasses(List<String> srcClasses) {
		this.srcClasses = srcClasses;
	}

	public Map<String, ClassNode> getClassVarMap() {
		return classVarMap;
	}

	public void setClassVarMap(Map<String, ClassNode> classVarMap) {
		this.classVarMap = classVarMap;
	}
	
	public void printClassVarMap() {
		for(Map.Entry<String, ClassNode> entry : classVarMap.entrySet()){
//			System.out.println();
			logger.info(entry.getValue().toString());
		}
	}
}
