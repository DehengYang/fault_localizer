/**
 * 
 */
package apr.apr.repair.localization;

/**
 * support GZoltar 1.7.3
 * @author apr
 * @version Apr 1, 2020
 *
 */
public class FaultLocalizer2 {
	// parameters we need
	private String data_dir;
	
	// test:
	// 1) twice execution (if we need to delete the datadir)
	// 2) other benchmark bugs
	

	public String getData_dir() {
		return data_dir;
	}

	public void setData_dir(String data_dir) {
		this.data_dir = data_dir;
	}
	
}
