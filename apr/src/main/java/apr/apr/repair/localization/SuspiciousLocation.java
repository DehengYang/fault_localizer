package apr.apr.repair.localization;

import java.util.ArrayList;
import java.util.List;

public class SuspiciousLocation {
	private String className;
	private int lineNo;
	private int execPassed; 
	private int execFailed; 
	private int totalPassed; 
	private int totalFailed;
	
	List<String> execPassedMethods = new ArrayList<>(); 
	List<String> execFailedMethods = new ArrayList<>();
	
	private double suspValue;
	
//	public SuspiciousLocation(String className, int lineNo){
//		this.setClassName(className);
//		this.setLineNo(lineNo);
//	}
//	
//	public SuspiciousLocation(String className, int lineNo, double suspValue){
//		this.setClassName(className);
//		this.setLineNo(lineNo);
//		this.setSuspValue(suspValue);
//	}

	public SuspiciousLocation(String className, int lineNo, double suspValue){
		this.className = className;
		this.lineNo = lineNo;
		this.suspValue = suspValue;
	}
	
	/**
	 * @param execPassed
	 * @param execFailed
	 * @param totalPassed
	 * @param totalFailed
	 * @param execPassedMethods
	 * @param execFailedMethods
	 */
	public SuspiciousLocation(String className, int lineNo, int execPassed, int execFailed, int totalPassed, int totalFailed,
			List<String> execPassedMethods, List<String> execFailedMethods) {
		this.className = className;
		this.lineNo = lineNo;
		this.execFailed = execFailed;
		this.execPassed = execPassed;
		this.setTotalFailed(totalFailed);
		this.setTotalPassed(totalPassed);
		
		this.execPassedMethods.addAll(execPassedMethods);
		this.execFailedMethods.addAll(execFailedMethods);
		
		this.suspValue = calculateSuspicious();
	}
	
	@Override
	public String toString(){
		return String.format("%s:%s,%s", this.className, this.lineNo, this.suspValue);
	}
	
	/**
	 * @Description calculate suspiciousness using Ochiai formula 
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 * @return
	 */
	public double calculateSuspicious(){
		return execFailed/Math.sqrt((execFailed+execPassed)*(execFailed+execPassed));
	}
	
	public double getSuspValue() {
		return suspValue;
	}
	public void setSuspValue(double suspValue) {
		this.suspValue = suspValue;
	}

	/**
	 * @Description getter and setter methods. 
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public int getTotalPassed() {
		return totalPassed;
	}

	public void setTotalPassed(int totalPassed) {
		this.totalPassed = totalPassed;
	}

	public int getTotalFailed() {
		return totalFailed;
	}

	public void setTotalFailed(int totalFailed) {
		this.totalFailed = totalFailed;
	}
}
