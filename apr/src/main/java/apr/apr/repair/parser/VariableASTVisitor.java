/**
 * 
 */
package apr.apr.repair.parser;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.*;
//import org.eclipse.jdt.core.dom.ASTVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author apr
 * @version Mar 19, 2020
 *
 */
public class VariableASTVisitor extends ASTVisitor {
	final static Logger logger = LoggerFactory.getLogger(VariableASTVisitor.class);
	
	private ASTNode node;
	private CompilationUnit cu;
	private int startLineNo;
	private int endLineNo;
	
	VariableASTVisitor(ASTNode node, CompilationUnit cu){
		this.node = node;
		this.cu = cu;
		
		startLineNo = getStart(node, cu);
		endLineNo = getEnd(node, cu);
	}
	
	/**
	 * @Description get start line number of the node 
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @return
	 */
	public int getStart(ASTNode node, CompilationUnit cu){
		return cu.getLineNumber(node.getStartPosition());
	}
	public int getEnd(ASTNode node, CompilationUnit cu){
		return cu.getLineNumber(node.getStartPosition() + node.getLength());
	}
	
	/**
	 * @Description get variable(s) of a node
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @param node
	 * @return
	 */
	public boolean getVariables(ASTNode node){
		int startLineNo = getStart(node, cu);
		int endLineNo = getEnd(node, cu);
		
		// do further search while in the node
		if (startLineNo >= this.startLineNo && endLineNo <= this.endLineNo){
			
			return true;
		}else{
//			return true;  //test
			return false; // stop as the sub-nodes do not contain the target lineNo
		}
	}
	
	@Override
	public void preVisit(ASTNode node){
		for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			IVariableBinding binding = fragment.resolveBinding();
			VariableBindingManager manager = new VariableBindingManager(fragment);
//			localVariableManagers.put(binding, manager);
		}
	}
	
}
