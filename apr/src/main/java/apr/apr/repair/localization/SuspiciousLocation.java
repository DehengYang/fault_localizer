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
	
	private List<String> execPassedMethods = new ArrayList<>(); 
	private List<String> execFailedMethods = new ArrayList<>();
	
	private List<Integer> coveredTestIndexList = null;
	
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

	public SuspiciousLocation(String className, int lineNo){
		this.className = className;
		this.lineNo = lineNo;
	}
	
	public SuspiciousLocation(String className, int lineNo, double suspValue){
		this.className = className;
		this.lineNo = lineNo;
		this.suspValue = suspValue;
	}
	
	/**
	 * 
	 * @param className
	 * @param lineNo
	 * @param execPassed
	 * @param execFailed
	 * @param totalPassed
	 * @param totalFailed
	 */
	public SuspiciousLocation(String className, int lineNo, int execPassed, int execFailed, int totalPassed, int totalFailed) {
		this.className = className;
		this.lineNo = lineNo;
		this.execFailed = execFailed;
		this.execPassed = execPassed;
		this.setTotalFailed(totalFailed);
		this.setTotalPassed(totalPassed);
		
		this.suspValue = calculateSuspicious();
	}
	
	/**
	 * support executed test record
	 * @param className
	 * @param lineNo
	 * @param execPassed
	 * @param execFailed
	 * @param totalPassed
	 * @param totalFailed
	 */
	public SuspiciousLocation(String className, int lineNo, int execPassed, int execFailed, int totalPassed, int totalFailed, List<Integer> coveredTestIndexList) {
		this.className = className;
		this.lineNo = lineNo;
		this.execFailed = execFailed;
		this.execPassed = execPassed;
		this.setTotalFailed(totalFailed);
		this.setTotalPassed(totalPassed);
		
		this.setCoveredTestIndexList(coveredTestIndexList);
		
		this.suspValue = calculateSuspicious();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return getName(this.getClassName()).equals(((SuspiciousLocation) o).getClassName())  &&  
    		   this.getLineNo()     ==    ((SuspiciousLocation) o).getLineNo();
    }
	
    /**
     * @Description this is to get rid of $ character.
     * e.g., spoon.generating.CloneVisitorGenerator$2$1:421,0.0    contains two $
spoon.support.compiler.jdt.JDTCommentBuilder$1:219,0.0 contains 1 $
     * @author apr
     * @version Apr 12, 2020
     *
     * @param fullClassName
     * @return
     */
    public String getName(String fullClassName){
    	if(fullClassName.contains("$")){
    		return fullClassName.split("$")[0];
    	}else{
    		return fullClassName;
    	}
    }
    
	/**
	 * @Description calculate suspiciousness using Ochiai formula 
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 * @return
	 */
	public double calculateSuspicious(){
		if (execFailed+execPassed == 0 || totalFailed == 0){
			return 0;
		}
		return execFailed/Math.sqrt((execFailed+execPassed)*totalFailed);
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

	public List<Integer> getCoveredTestIndexList() {
		return coveredTestIndexList;
	}

	public void setCoveredTestIndexList(List<Integer> coveredTestIndexList) {
		this.coveredTestIndexList = coveredTestIndexList;
	}
}
