/**
 * 
 */
package apr.apr.repair.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author apr
 * @version Mar 18, 2020
 *
 */
public class CodeSnippet {
	List<ASTNode> nodes = new ArrayList<>();
	int startLineNo;
	int endLineNo;
	
	final static Logger logger = LoggerFactory.getLogger(CodeSnippet.class);
	
	public void addNode(ASTNode node){
		nodes.add(node);
	}
}
