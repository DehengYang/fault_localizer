/**
 * 
 */
package apr.apr.repair.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.apache.commons.codec.digest.MurmurHash3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import apr.apr.repair.parser.CodeBlocks;
import apr.apr.repair.parser.CodeFragment;

/**
 * @author apr
 * @version Mar 23, 2020
 *
 */
public class NodeUtil {
	final static Logger logger = LoggerFactory.getLogger(NodeUtil.class);
	
	public static String getPrettyPrintNode(Node node){
		PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
//		conf.setIndentSize(" ");
		conf.setPrintComments(false);
		PrettyPrinter prettyPrinter = new PrettyPrinter(conf);
		return prettyPrinter.print(node);
	}
	
	public static <T> int getIndex(List<T> nodes, Node node){
		int index = -1;
//		int index = siblings.indexOf(curNode);  // debug: when two same nodes in a parent -> verified. need to change
		for(int i = 0; i < nodes.size(); i++){
			Node n = (Node) nodes.get(i);
			if (n.equals(node) && n.getRange().get().equals(node.getRange().get())){
				index = i;
//				sib.getTokenRange().get();
			}
		}
		return index;
	}
	
	
	
	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * e.g., 
	 * if (colorings.isEmpty() || !n.isName() || parent.isFunction()) {
     *     return;
	 * }
	 * -> 
	 * 
	 * if (var.id() || !n.isName() || parent.isFunction()) {
	 * 
	 *
	 * @param targetNode2
	 * @return
	 */
	public static Node tokenize(Node targetNode) {
		Node node = targetNode.clone();
		// identifiers, literals, and variable types
		// id: ClassOrInterfaceType, varName, methodName
		// lit: all literalExpr
//		node.findAll(ClassOrInterfaceType.class);
//		node.findAll(VariableDeclarator.class);
//		node.findAll(FieldDeclaration.class);
//		node.findAll(MethodDeclaration.class);
//		node.findAll(FieldAccessExpr.class);
//		node.findAll(MethodCallExpr.class);
		
		// a simple approach
		Consumer<Node> consumer = n -> {
			if (n instanceof SimpleName){
				((SimpleName) n).setIdentifier("id");
			}
			
			if (n instanceof ClassOrInterfaceType){
				((ClassOrInterfaceType) n).setName("varType");
			}
			
			if (n instanceof LiteralExpr){
				n.replace(new StringLiteralExpr("literal"));
			}
		};
		node.walk(consumer);
		
		logger.info(getPrettyPrintNode(node));
		
		
		return node;
	}
	
	public static String getTokenizedStr(CodeFragment cf){
		List<Node> nodes = cf.getNodes();
		String str = "";
		for (Node node : nodes){
			Node tokenizedNode = tokenize(node);
			str += getPrettyPrintNode(tokenizedNode);
		}
		return str;
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 21, 2020
	 *
	 * @param n
	 * @return
	 */
	private static String getClassName(Node n) {
		int cnt = 0; // set a limit to avoid infinite loop
		Node parent = n.getParentNode().get();
		while(true){
			cnt ++;
			if (cnt >= 100000) return null;
			
			if (parent instanceof ClassOrInterfaceDeclaration){
				String className = ((ClassOrInterfaceDeclaration) parent).getName().asString();
				return className;
			}else{
				parent = parent.getParentNode().get();
			}
		}
	}
	
	/**
	 * @Description get start line number of the node 
	 * @author apr
	 * @version Mar 20, 2020
	 *
	 * @return
	 */
	public static int getStartLineNo(Node node){
		Range range = node.getRange().get();
		return range.begin.line;
	}
	
	
	public static int getEndLineNo(Node node){
		Range range = node.getRange().get();
		return range.end.line;
	}
	
	/** @Description 
	 * @author apr
	 * @version Mar 22, 2020
	 *
	 * @param preSib
	 * @return
	 */
	public static int getLineRange(Node node) {
		return getEndLineNo(node) - getStartLineNo(node) + 1;
	}

	/** @Description 
	 * @author apr
	 * @version Mar 23, 2020
	 *
	 * @param cf
	 * @param cbs
	 */
	public static void getSimilarCode(CodeFragment cf, CodeBlocks cbs) {
		List<CodeFragment> codeFrags = cbs.getCodeFrags();
		
		// constants
		int windowSize = 6; // q
		int e = 1; // mis-match
		double simThreshold = 0.7;
		
		String[] tokenLines = getTokenizedStr(cf).split("\n");
		
		if (tokenLines.length <= 6){
			windowSize = (int) Math.ceil((double) cf.getLineRangeSize()/2);
		}
		
		
		// hash map
		Map<CodeFragment, Map<long[], Integer>> hashSet = new HashMap<>();
		Map<long[], Integer> hashSubSet = new HashMap<>();
		Map<long[], List<CodeFragment>> candMap = new HashMap<>(); // candidates map
		
		codeFrags.add(cf);
		
		for (CodeFragment codeFrag : codeFrags){
			// jude if similar/clone
			// refer to: "CCAligner: A token based large-gap clone detector"
			String[] tLines = getTokenizedStr(codeFrag).split("\n");
			int tSize = tLines.length;
			int numWindows = tSize - windowSize + 1;
			for (int i = 0; i < numWindows; i++){
				String tLine = tLines[i];
				int[] indexes = new int[windowSize]; // i -> i + windowSize - 1
				for (int ind = 0; i < windowSize; ind++){
					indexes[ind] = ind + i;
				}
				
				// refer to: https://www.cnblogs.com/zzlback/p/10947064.html
				List<List<Integer>> indList = new ArrayList<>();
				diffCombine(indexes,windowSize - e, 0, 0, indList);
				
				// 
				for (List<Integer> ind : indList){
					String combinedLines = "";
					for (int indTemp : ind){
						combinedLines += tLines[indTemp];
					}
					
					long[] hashK = MurmurHash3.hash128(combinedLines.getBytes());
					CodeFragment hashV = codeFrag;
					hashSubSet.put(hashK, i);
					
					if (candMap.containsKey(hashK)){
						candMap.get(hashK).add(hashV);
					}else{
						List<CodeFragment> vList = new ArrayList<>();
						vList.add(hashV);
						candMap.put(hashK, vList);
					}
				}
			}
			// hashSubSet
			hashSet.put(codeFrag, hashSubSet);
			hashSubSet.clear();
			
		}
			
		// deal with cf.
		
	}
	
	public static Stack<Integer> stack = new Stack<Integer>();
	
	/**
	 * @Description refer to: https://www.cnblogs.com/zzlback/p/10947064.html 
	 * @author apr
	 * @version Mar 23, 2020
	 *
	 * @param indexes
	 * @param targ
	 * @param has
	 * @param cur
	 */
	public static void diffCombine(int[] indexes, int targ, int has, int cur, List<List<Integer>> indList ) {
		
        if(has == targ) {
        	List<Integer> ind = new ArrayList<>();
        	for (int i : stack){
        		ind.add(i);
        	}
        	ind.sort(null);
        	indList.add(ind);
//            System.out.println(stack);
            return;
        }
         
        for(int i=cur;i<indexes.length;i++) {
            if(!stack.contains(indexes[i])) {
                stack.add(indexes[i]);
                diffCombine(indexes, targ, has+1, i, indList);
                stack.pop();
            }
        }
    }
	
	
}
