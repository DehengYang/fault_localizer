/**
 * 
 */
package apr.apr.repair.parser;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;

/**
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class ClassNode {
	final static Logger logger = LoggerFactory.getLogger(ClassNode.class);
	
	private String className;
	private String fullClassName; 
	private Map<String, String> varMap = new HashMap<>();  // varname, vartype
	
	public ClassNode(String className, String fullClassName, Map<String, String> varMap){
		this.setClassName(className);
		this.setFullClassName(fullClassName);
		
		for (Map.Entry<String, String> entry : varMap.entrySet()){
			this.varMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	public void printVarMap(){
		System.out.format("className: {}", className);
		for (Map.Entry<String, String> entry : varMap.entrySet()){
			System.out.format("var: %s, type: %s",entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String toString(){
		String str = String.format("className: %s, fullClassName: %s\n", className, fullClassName) ;
		for (Map.Entry<String, String> entry : varMap.entrySet()){
			str += String.format("var: %s, type: %s\n",entry.getKey(), entry.getValue());
		}
		return str;
	}
	
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, String> getVarMap() {
		return varMap;
	}

	public void setVarMap(Map<String, String> varMap) {
		this.varMap = varMap;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}
	
	

	
	
}
