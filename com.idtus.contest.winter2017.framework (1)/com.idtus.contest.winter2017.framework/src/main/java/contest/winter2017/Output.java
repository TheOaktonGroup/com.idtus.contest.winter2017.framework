package contest.winter2017;

/**
 * Class to hold output (std out/err) associated with a given test run 
 * 
 * @author IDT
 */
public class Output {

	/**
	 * String of the standard out associated with a given test run
	 */
	private String stdOutString = null;
	
	/**
	 * String of a standard error associated with a given test run 
	 */
	private String stdErrString = null;
	
	
	/**
	 * Ctr for Output object
	 * @param stdOutString - std out string to hold
	 * @param stdErrString - std err string to hold
	 */
	public Output(String stdOutString, String stdErrString) {
		this.stdOutString = stdOutString;
		this.stdErrString = stdErrString;
	}
	
	
	/**
	 * Getter for std out string
	 * @return String representation of std out associated with a given test run
	 */
	public String getStdOutString() {
		return stdOutString;
	}

	
	/**
	 * Getter for std err string
	 * @return String representation of std err associated with a given test run
	 */
	public String getStdErrString() {
		return stdErrString;
	}
	
}
