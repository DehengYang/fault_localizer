/**
 * 
 */
package apr.apr.repair.parser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;

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
	
	private int windowsSize = 10;
	private int fragSize = 10; // at least 10 lines for a code fragment
	
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
	
}
