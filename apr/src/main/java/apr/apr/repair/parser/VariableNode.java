/**
 * 
 */
package apr.apr.repair.parser;

import com.github.javaparser.ast.Node;

/**
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class VariableNode {
	private Node node;
	private String varName;
	private String varOriType;
	private String varRefType;
	
	public VariableNode(Node node, String varName, String varOriType){
		this(node, varName, varOriType, "");
	}
	
	public VariableNode(Node node, String varName, String varOriType, String varRefType){
		this.setNode(node);
		this.setVarName(varName);
		this.setVarOriType(varOriType);
		this.setVarRefType(varRefType);
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
		return String.format("Variable: %s, name: %s, oriType: %s, refType: %s", node.toString(), varName, varOriType, varRefType);
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
