package contest.winter2017;

import java.util.List;
import java.util.Map;

/**
 * Class that represents a basic test that is extracted from the executable jar and then run against the executable jar
 * to determine a pass/fail status. 
 * 
 * @author IDT
 */
public class Test {
	
	/**
	 * List of parameter values that will be passed into the executable jar as 
	 * as single test
	 */
	@SuppressWarnings("rawtypes")
	private List parameters;
	
	/**
	 * The regex string that describes the expected std out result for the test
	 */
	private String stdOutExpectedResultRegex;
	
	/**
	 * The regex string that describes the expected std err result for the test
	 */
	private String stdErrExpectedResultRegex;
	

	/**
	 * Ctr for Test object
	 * @param inputMap
	 */
	@SuppressWarnings("rawtypes")
	public Test(Map inputMap) {
		this.parameters = (List)inputMap.get("parameters");
		this.stdOutExpectedResultRegex = (String)inputMap.get("stdOutExpectedResultRegex");
		this.stdErrExpectedResultRegex = (String)inputMap.get("stdErrExpectedResultRegex");
	}
	
	
	/**
	 * Getter for parameters List
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getParameters() {
		return parameters;
	}

	
	/**
	 * Getter for Standard Out expected results regex
	 * @return
	 */
	public String getStdOutExpectedResultRegex() {
		return stdOutExpectedResultRegex;
	}

	
	/**
	 * Getter for Standard Error expected results regex
	 * @return
	 */
	public String getStdErrExpectedResultRegex() {
		return stdErrExpectedResultRegex;
	}
	
}
