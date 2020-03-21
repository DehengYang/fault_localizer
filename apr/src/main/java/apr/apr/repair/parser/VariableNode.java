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
		return String.format("Variable: %s, name: %s, oriType: %s, refType: %s, range: %s-%s", node.toString(), varName, varOriType, varRefType,
				node.getRange().get().begin.line, node.getRange().get().end.line);
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof VariableNode){
			VariableNode vn = (VariableNode) obj;
			return node.equals(vn.getNode());
		}
		return false;
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
