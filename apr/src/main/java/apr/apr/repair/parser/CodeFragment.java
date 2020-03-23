/**
 * 
 */
package apr.apr.repair.parser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;

import apr.apr.repair.utils.NodeUtil;

/**
 * @author apr
 * @version Mar 22, 2020
 *
 */
public class CodeFragment {
	final static Logger logger = LoggerFactory.getLogger(CodeFragment.class);
	
	private List<Node> nodes = new ArrayList<>();
	private int lineNo;
	private String className;
	private String directory; 
	
	private int windowsSize = 6;
	private int fragSize = 10; // at least 10 lines for a code fragment
	
	public CodeFragment(){
	}
	
	public CodeFragment(Node node){
		nodes.add(node);
	}
	
	public CodeFragment(List<Node> nodes){
		this.setNodes(nodes);
	}
	
	/**
	 * @Description get how many lines the CodeFragment has. 
	 * @author apr
	 * @version Mar 23, 2020
	 *
	 * @return
	 */
	public int getLineRangeSize(){
		if (nodes.isEmpty()) return 0;
		
		return NodeUtil.getEndLineNo(nodes.get(nodes.size()-1)) - NodeUtil.getStartLineNo(nodes.get(0)) + 1;
	}
	
	public CodeFragment(int lineNo, String className, String directory){
		this.setLineNo(lineNo);
		this.setClassName(className);
		this.setDirectory(directory);
		
		NodeFinder nf = new NodeFinder(lineNo, className, directory);
		Node targetNode = nf.findTargetNode();
		List<Node> nodes = nf.getCodeFragment(fragSize, targetNode);
		printCodeFragment(nodes);
		logger.debug("");
	}
	
	public void printCodeFragment(List<Node> nodes){
		System.out.println("print the code fragment:");
		for (Node node : nodes){
			System.out.println(node.toString());
		}
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes.addAll(nodes);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/** @Description 
	 * @author apr
	 * @version Mar 23, 2020
	 *
	 * @param stmti
	 */
	public void addNode(Node node) {
		nodes.add(node);
	}
	
}
