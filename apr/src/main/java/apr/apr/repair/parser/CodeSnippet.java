/**
 * 
 */
package apr.apr.repair.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author apr
 * @version Mar 18, 2020
 *
 */
public class CodeSnippet {
	List<ASTNode> children = new ArrayList<ASTNode>();
	
	List<ASTNode> nodes = new ArrayList<>();
	int startLineNo;
	int endLineNo;
	
	List<ASTNode> variables = new ArrayList<>();
	
	final static Logger logger = LoggerFactory.getLogger(CodeSnippet.class);
	
	public CodeSnippet(ASTNode node){
		nodes.add(node);
	}
	
	public void addNode(ASTNode node){
		nodes.add(node);
	}
	
	public void getVariables(){
		for(ASTNode node : nodes){
//			List<ASTNode> children = getChildren(node);
			getChildren(node.getParent());
			logger.debug("");
		}
	}
	
//	public List<ASTNode> getChildren(ASTNode node){
//		List<ASTNode> children = new ArrayList<ASTNode>();
//		
////		ExpressionStatementVisitor visitor = new ExpressionStatementVisitor();
//		
//		return children;
//	}
	
	/**
	 * @Description https://stackoverflow.com/questions/40547714/how-to-find-all-sub-nodes-children-and-childrens-children-of-an-astnode 
	 * @author apr
	 * @version Mar 19, 2020
	 *
	 * @param node
	 */
	public static void getChildren(ASTNode node) {
	    if (node != null) {
	        List<ASTNode> children = new ArrayList<ASTNode>();
	        List list = node.structuralPropertiesForType();
	        for (int i = 0; i < list.size(); i++) {
	            Object child = node.getStructuralProperty((StructuralPropertyDescriptor) list.get(i));
	            if (child instanceof ASTNode) {
	                children.add((ASTNode) child);
	            }               
	        }
	        for(ASTNode child : children){
	            if (child != null) {
//	                String c = children.toString();
	                logger.info("Children Node:\n" + child.toString() + "\n");
	                getChildren(child);
	            } 
	        }
	    }else {
	        return; 
	    }       
	}
	
	/**
	 * How to use visitor for an AST in java -> https://stackoverflow.com/questions/10331641/how-to-use-visitor-for-an-ast-in-java
	 * Children of org.eclipse.jdt.core.dom.ASTNode -> https://stackoverflow.com/questions/11841789/children-of-org-eclipse-jdt-core-dom-astnode/11853270#11853270
	 * What does the accept() method of ASTNode do and how does it use the ASTVisitor? -> https://stackoverflow.com/questions/4324820/what-does-the-accept-method-of-astnode-do-and-how-does-it-use-the-astvisitor
	 * 
	 */
//	public List<ASTNode> getChildren(ASTNode node){
//		List<ASTNode> children = new ArrayList<ASTNode>();
//		
//		logger.info("node name: {}\nnode class:{}\n", node.toString(), node.getClass());
//		
//		List<?> list = node.structuralPropertiesForType();//Returns a list of structural property descriptors for nodes of the same type as this node. 
//		for (int i = 0; i < list.size(); i++){
//			Object child = node.getStructuralProperty( (StructuralPropertyDescriptor) list.get(i) );
//			logger.debug("child name: {}\nchild class:{}\n", child.toString(), child.getClass());
//			
//			if (child instanceof ASTNode) {
////				logger.info("child name: {}\nchild class:{}\n", child.toString(), child.getClass());
//	            children.add((ASTNode) child);
//	        }
//		}
//		
//		return children;
//	}
}
