/**
 * 
 */
package apr.apr.repair.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;

/**
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class VariableNode {
	final static Logger logger = LoggerFactory.getLogger(VariableNode.class);
	
	private Node node;
	private String varName;
	private String varOriType;
	private String varRefType;
	
	private int beginLineNo;
	private int endLineNo;
	
	public VariableNode(Node node, String varName, String varOriType){
		this(node, varName, varOriType, "");
	}
	
	public VariableNode(Node node, String varName, String varOriType, String varRefType){
		this.setNode(node);
		this.setVarName(varName);
		this.setVarOriType(varOriType);
		this.setVarRefType(varRefType);
		
		beginLineNo = node.getRange().get().begin.line;
		endLineNo = node.getRange().get().end.line;
	}

	/**
	 * @Description formmat print  
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	@Override
	public String toString(){
		return String.format("Variable: %s, name: %s, oriType: %s, refType: %s, range: %s-%s, class: %s", node.toString(), varName, varOriType, varRefType,
				beginLineNo, endLineNo, node.getClass());
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof VariableNode){
			VariableNode vn = (VariableNode) obj;
			boolean equal = node.equals(vn.getNode());
			if (equal){
				logger.debug("repeated info: {}, {}, {}, {}.",this.varName, vn.getVarName(), this.beginLineNo, vn.getBeginLineNo());
				// may not be the totoal same location node.
				//2020-03-21 01:07:10 DEBUG VariableNode:60 - repeated info: t, t, 144, 113.
			}
			return equal;
		}
		return false;
	}
	
	public int getBeginLineNo() {
		return beginLineNo;
	}

	public void setBeginLineNo(int beginLineNo) {
		this.beginLineNo = beginLineNo;
	}
	
	public int getEndLineNo() {
		return endLineNo;
	}

	public void setEndLineNo(int endLineNo) {
		this.endLineNo = endLineNo;
	}

	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getVarOriType() {
		return varOriType;
	}

	public void setVarOriType(String varOriType) {
		this.varOriType = varOriType;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarRefType() {
		return varRefType;
	}

	public void setVarRefType(String varRefType) {
		this.varRefType = varRefType;
	}
	
}
