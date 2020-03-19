/**
 * 
 */
package apr.apr.repair.parser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** refer to: https://github.com/search?l=Java&q=%22extends+astvisitor%22&type=Code
 * https://github.com/dearzhaorui/vnf-modeling/blob/58cf016228db6a401c35ee40fc86cfbefe48385f/NFDev/src/it/polito/parser/ReturnStatementVisitor.java
 * 
 * refs:
 * [Use JDT ASTParser to Parse Single .java files](https://www.programcreek.com/2011/11/use-jdt-astparser-to-parse-java-file/)
 * [Class ASTVisitor](https://help.eclipse.org/2019-12/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTVisitor.html)
 * https://www.vogella.com/tutorials/EclipseJDT/article.html#abstract-syntax-tree-ast
 * https://www.eclipse.org/articles/Article-JavaCodeManipulation_AST/
 * 
 * @author apr
 * @version Mar 18, 2020
 *
 */
public class StmtASTVistor extends ASTVisitor {
	final static Logger logger = LoggerFactory.getLogger(StmtASTVistor.class);
	
	private int lineNo;
	private ASTNode stmtNode;
	private CompilationUnit cu;
	
	private int validLineNo;
	
	StmtASTVistor(int lineNo, CompilationUnit cu){
		this.lineNo = lineNo;
		this.cu = cu;
	}
	
	/**
	 * @Description sometimes the stmtNode can be Declaration rather than Statement node. 
	 * @author apr
	 * @version Mar 18, 2020
	 *
	 * @return
	 */
	public ASTNode getStatementNode(){
		return stmtNode;
	}
	
	public int getValidLineNo(){
		return validLineNo;
	}
	
	// all types
//	AssertStatement,
//    Block,
//    BreakStatement,
//    ConstructorInvocation,
//    ContinueStatement,
//    DoStatement,
//    EmptyStatement,
//    EnhancedForStatement
//    ExpressionStatement,
//    ForStatement,
//    IfStatement,
//    LabeledStatement,
//    ReturnStatement,
//    SuperConstructorInvocation,
//    SwitchCase,
//    SwitchStatement,
//    SynchronizedStatement,
//    ThrowStatement,
//    TryStatement,
//    TypeDeclarationStatement,
//    VariableDeclarationStatement,
//    WhileStatement
	
	/**
	 * @Description find the validLineNo & the corresponding node. 
	 * 
	 * refs:
	 * [eclipse ASTNode to source code line number](https://stackoverflow.com/questions/11126857/eclipse-astnode-to-source-code-line-number)
	 * https://stackoverflow.com/questions/28400127/get-line-number-of-a-methoddeclaration
	 * & javadoc
	 * 
	 * @author apr
	 * @version Mar 18, 2020
	 *
	 * @param node
	 * @return
	 */
	public boolean findLineNo(ASTNode node){
		// get node info
//		System.out.println(node.toString());
//		System.out.println(node.getClass());
//		logger.info("node str:\n{}node class:\n{}\nnode parent:{}\n\n", node.toString(), node.getClass(), node.getParent());
		
		int startLineNo = cu.getLineNumber(node.getStartPosition()); // returns 1-based line number
		int endLineNo = cu.getLineNumber(node.getStartPosition() + node.getLength());
		
		if (startLineNo <= lineNo && endLineNo >= lineNo){
			validLineNo = startLineNo;
			stmtNode = node; //(Statement) 
			return true;
		}else{
//			return true;  //test
			return false; // stop as the sub-nodes do not contain the target lineNo
		}
	}
	
	/**
	 * extra visits
	 */
	@Override
	public boolean visit(FieldDeclaration node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(TypeDeclaration node){
		return findLineNo(node);
	}
	
	//FieldDeclaration private var
	//MethodDeclaration method
	//TypeDeclaration   class    //Javadoc
	
	// to get children nodes of an AST node
	// refer to: https://stackoverflow.com/questions/11841789/children-of-org-eclipse-jdt-core-dom-astnode/11853270#11853270
	// wget http://archive.eclipse.org/jdt/ui/update-site/plugins/org.eclipse.jdt.astview_1.1.9.201406161921.jar
	// https://www.eclipse.org/jdt/ui/astview/index.php
	
	@Override
	public void preVisit(ASTNode node){
		// get info
//		if (node instanceof CompilationUnit) return;
//		logger.debug("--node str:\n{}\n--node class:\n{}\n--node parent:\n{}\n\n", node.toString(), node.getClass(), node.getParent());
		
//		if(node.toString().contains("import java.util.Set;")){
//			System.out.println();
//		}
		
//		System.out.println("\n---node info---");
//		System.out.println(node.toString());
//		System.out.println(node.getClass());
	}
	
	/**
	 * for all statement types
	 */
	@Override
	public boolean visit(AssertStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(Block node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(BreakStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ConstructorInvocation node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ContinueStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(DoStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(EmptyStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(EnhancedForStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ExpressionStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ForStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(IfStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(LabeledStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ReturnStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(SwitchCase node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(SwitchStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(SynchronizedStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(ThrowStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(TryStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(TypeDeclarationStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node){
		return findLineNo(node);
	}
	
	@Override
	public boolean visit(WhileStatement node){
		return findLineNo(node);
	}
}
