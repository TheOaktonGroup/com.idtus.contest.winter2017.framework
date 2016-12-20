package contest.winter2017;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Class that will handle execution of basic tests and exploratory security test on a black-box executable jar.  
 * 
 * Example code that we used to guide our use of Jacoco code coverage was found @ http://www.eclemma.org/jacoco/trunk/doc/api.html
 * 
 * @author IDT
 */
public class Tester {

	
	/**
	 * suffix for all jacoco output files
	 */
	private static final String JACOCO_OUTPUT_FILE_SUFFIX = "_jacoco.exec";
	
	/**
	 * horizontal line shown between test output
	 */
	private static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------";
	
	/**
	 * path of the jar to test as a String
	 */
	private String jarToTestPath = null;
	
	/**
	 * path of the directory for jacoco output as a String
	 */
	private String jacocoOutputDirPath = null;
	
	/**
	 * path to the jacoco agent library as a String
	 */
	private String jacocoAgentJarPath = null;
	
	/**
	 * path to the file for jacoco output as a String
	 */
	private String jacocoOutputFilePath = null;
	
	/**
	 * basic tests that have been extracted from the jar under test
	 */
	private List<Test> tests = null;
	
	/**
	 * parameter factory that can be used to help figure out parameter signatures from the blackbox jars
	 */
	private ParameterFactory parameterFactory = null;
	
	
	//////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////
	
	/**
	 * Method that will initialize the Framework by loading up the jar to test, and then extracting
	 * parameters, parameter bounds (if any), and basic tests from the jar.
	 * 
	 * @param initJarToTestPath - String representing path of the jar to test
	 * @param initJacocoOutputDirPath - String representing path of the directory jacoco will use for output
	 * @param initJacocoAgentJarPath - String representing path of the jacoco agent jar
	 * @return boolean - false if initialization encounters an Exception, true if it does not
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean init(String initJarToTestPath, String initJacocoOutputDirPath, String initJacocoAgentJarPath) {
		
		this.jarToTestPath = initJarToTestPath;
		this.jacocoOutputDirPath = initJacocoOutputDirPath;
		this.jacocoAgentJarPath = initJacocoAgentJarPath;
		
		File jarFileToTest = new File(this.jarToTestPath);
		this.jacocoOutputFilePath = this.jacocoOutputDirPath+"\\"+jarFileToTest.getName().replaceAll("\\.", "_")+JACOCO_OUTPUT_FILE_SUFFIX;
		
		File jacocoOutputFile = new File(this.jacocoOutputFilePath);
		if (jacocoOutputFile !=null && jacocoOutputFile.exists()) {
			jacocoOutputFile.delete();
		}
		
		URL fileURL = null;
	    URL jarURL = null;
		try {
			
			// load up the jar under test so that we can access information about the class from 'TestBounds'
			fileURL = jarFileToTest.toURI().toURL();
			String jarUrlTemp = "jar:"+jarFileToTest.toURI().toString()+"!/";
			jarURL = new URL(jarUrlTemp);
			URLClassLoader cl = URLClassLoader.newInstance(new URL[]{fileURL});
			JarURLConnection jarURLconn = null;
			jarURLconn = (JarURLConnection)jarURL.openConnection();

			// figuring out where the entry-point (main class) is in the jar under test
			Attributes attr = null;
			attr = jarURLconn.getMainAttributes();
			String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);
			
			// loading the TestBounds class from the jar under test
			String mainClassTestBoundsName = mainClassName+"TestBounds";
			Class<?> mainClassTestBounds = null;
			try {
				mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// use reflection to invoke the TestBounds class to get the usage information from the jar
			Method testBoundsMethod = null;
			testBoundsMethod = mainClassTestBounds.getMethod("testBounds");
			
			Object mainClassTestBoundsInstance = null;
			mainClassTestBoundsInstance = mainClassTestBounds.newInstance();

			Map<String, Object> mainClassTestBoundsMap = null;
			mainClassTestBoundsMap =
					(Map<String, Object>)testBoundsMethod.invoke(mainClassTestBoundsInstance);

			// instantiating a new Parameter Factory using the Test Bounds map
			this.parameterFactory = new ParameterFactory(mainClassTestBoundsMap);
			
			// get a list of basic tests from the TestBounds class
			this.tests = new ArrayList<Test>();
			List testList = (List)mainClassTestBoundsMap.get("tests");
			for(Object inTest : testList) {
				this.tests.add(new Test((Map)inTest));
			}

		} catch (IOException|IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
				InstantiationException | NoSuchMethodException | SecurityException | NullPointerException e) {
			// if we have an exception during initialization, display the error to the user and return a false status
			System.out.println("ERROR: An exception occurred during initialization.");
			e.printStackTrace();
			return false;
		} 
		
		// if we did not encounter an exception during initialization, return a true status
		return true;
	}
	
	
	/**
	 * This is the half of the framework that IDT has completed. We are able to pull basic tests 
	 * directly from the executable jar. We are able to run the tests and assess the output as PASS/FAIL.
	 * 
	 * You likely do not have to change this part of the framework. We are considering this complete and 
	 * want your team to focus more on the SecurityTests.  
	 */
	public void executeBasicTests() {
		
		int passCount = 0;
		int failCount = 0;
		
		// iterate through the lists of tests and execute each one
		for(Test test : this.tests) {
			 
			// instrument the code to code coverage metrics, execute the test with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			printBasicTestOutput(output);
			
			// determine the result of the test based on expected output/error regex
			if(output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
					&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
				System.out.println("basic test result: PASS");
				passCount++;
			}
			else {
				System.out.println("basic test result: FAIL ");
				failCount++;
				
				// since we have a failed basic test, show the expectation for the stdout
				if(!output.getStdOutString().matches(test.getStdOutExpectedResultRegex())) {
					System.out.println("\t ->stdout: "+output.getStdOutString());
					System.out.println("\t ->did not match expected stdout regex: "+test.getStdOutExpectedResultRegex());
				}
				
				// since we have a failed basic test, show the expectation for the stderr
				if(!output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					System.out.println("\t ->stderr: "+output.getStdErrString());
					System.out.println("\t ->did not match expected stderr regex: "+test.getStdErrExpectedResultRegex());
					
				}
			}
			System.out.println(HORIZONTAL_LINE);
		} 
		// print the basic test results and the code coverage associated with the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		System.out.println("basic test results: " + (passCount + failCount) + " total, " + passCount + " pass, " + failCount + " fail, " + percentCovered + " percent covered");
		System.out.println(HORIZONTAL_LINE);
	}
	
	
	/**
	 * This is the half of the framework that IDT has not completed. We want you to implement your exploratory 
	 * security vulnerability testing here.
	 * 
	 * In an effort to demonstrate some of the features of the framework that you can already utilize, we have
	 * provided some example code in the method. The examples only demonstrate how to use existing functionality. 
	 */
	public void executeSecurityTests() {
		
		/////////// START EXAMPLE CODE /////////////
		
		// This example demonstrates how to use the ParameterFactory to figure out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the ParameterFactory class). As we figure out 
		// what each parameter type is, we are assigning it a simple (dumb) value so that we can use those parameters 
		// to execute the black-box jar. By the time we finish this example, we will have an array of concrete 
		// parameters that we can use to execute the black-box jar.
		List<String> previousParameterStrings = new ArrayList<String>(); // start with a blank parameter list since we are going to start with the first parameter
		List<Parameter> potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		Parameter potentialParameter;
		while (!potentialParameters.isEmpty()) {
			String parameterString = "";
			potentialParameter = potentialParameters.get(0); 
			
			//if(potentialParameter.isOptional())  //TODO? - your team might want to look at this flag and handle it as well!
			
			// an enumeration parameter is one that has multiple options
			if (potentialParameter.isEnumeration()) {
				parameterString = potentialParameter.getEnumerationValues().get(0) + " "; // dumb logic - given a list of options, always use the first one
				
				// if the parameter has internal format (eg. "<number>:<number>PM EST")
				if(potentialParameter.isFormatted()) {
					
					// loop over the areas of the format that must be replaced and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for(Class type :potentialParameter.getFormatVariables(parameterString)) {
						if (type == Integer.class){ 
							formatVariableValues.add(new Integer(1)); // dumb logic - always use '1' for an Integer
						} else if (type == String.class) {
							formatVariableValues.add(new String("one")); // dumb logic - always use 'one' for a String
						}
					}
					
					//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
					parameterString =
							potentialParameter.getFormattedParameter(
									parameterString, formatVariableValues);
				}
				previousParameterStrings.add(parameterString);
			// if it is not an enumeration parameter, it is either an Integer, Double, or String
			} else {
				if (potentialParameter.getType() == Integer.class){ 
					parameterString = Integer.toString(1) + " ";	// dumb logic - always use '1' for an Integer
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == Double.class) {
					parameterString = Double.toString(1.0) + " ";	// dumb logic - always use '1.0' for a Double
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == String.class) {

					// if the parameter has internal format (eg. "<number>:<number>PM EST")
					if(potentialParameter.isFormatted()) {

						// loop over the areas of the format that must be replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for(Class type : potentialParameter.getFormatVariables()) {
							if (type == Integer.class){ 
								formatVariableValues.add(new Integer(1)); // dumb logic - always use '1' for an Integer
							} else if (type == String.class) {
								formatVariableValues.add(new String("one")); // dumb logic - always use 'one' for a String
							}
						}
						
						//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
						parameterString =
								potentialParameter.getFormattedParameter(formatVariableValues);
					}
					else {
						parameterString = "one ";		// dumb logic - always use 'one' for a String
					}

					previousParameterStrings.add(parameterString);
				} else {
					parameterString = "unknown type";
				}
			}
			// because of the challenge associated with dependent parameters, we must go one parameter
			// at a time, building up the parameter list - getNext is the method that we are using 
			// to get the next set of options, given an accumulating parameter list. 
			potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		}
		Object[] parameters = previousParameterStrings.toArray();
		
		// This example demonstrates how to execute the black-box jar with concrete parameters
		// and how to access (print to screen) the standard output and error from the run
		Output output = instrumentAndExecuteCode(parameters);
		printBasicTestOutput(output); 
		
		// We do not intend for this example code to be part of your output. We are only
		// including the example to show you how you might tap into the code coverage
		// results that we are generating with jacoco
		showCodeCoverageResultsExample();

		/////////// END EXAMPLE CODE ////////////// 
		
	}
	
	
	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////
	
	/**
	 * This method will instrument and execute the jar under test with the supplied parameters.
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on the command line
	 * because the user has already set the appropriate environment variable path. 
	 * 
	 * @param parameters - array of Objects that represents the parameter values to use for this 
	 *                     execution of the jar under test
	 *                     
	 * @return Output representation of the standard out and standard error associated with the run
	 */
	private Output instrumentAndExecuteCode(Object[] parameters) {
			
		Process process = null;
		Output output = null;	
		
		// we are building up a command line statement that will use java -jar to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage metrics
		String command = "java";
		try {	
			command += " -javaagent:" + this.jacocoAgentJarPath + "=destfile=" + this.jacocoOutputFilePath;
			command += " -jar " + this.jarToTestPath;
			for (Object o: parameters) {
				command += " " + o.toString();
			}
			
			// show the user the command to run and prepare the process using the command
			System.out.println("command to run: "+command);
			process = Runtime.getRuntime().exec(command);
		
			// prepare the stream needed to capture standard output
			InputStream isOut = process.getInputStream();
			InputStreamReader isrOut = new InputStreamReader(isOut);
			BufferedReader brOut = new BufferedReader(isrOut);
			StringBuffer stdOutBuff = new StringBuffer();
			
			// prepare the stream needed to capture standard error
			InputStream isErr = process.getErrorStream();
			InputStreamReader isrErr = new InputStreamReader(isErr);
			BufferedReader brErr = new BufferedReader(isrErr);
			StringBuffer stdErrBuff = new StringBuffer();
			
			String line;
			boolean outDone = false;
			boolean errDone = false;
			
			// while standard out is not complete OR standard error is not complete
			// continue to probe the output/error streams for the applications output
			while(!outDone || !errDone) {
				
				// monitoring the standard output from the application
				boolean outReady = true;
				if(outReady) {
					line = brOut.readLine();
					if(line == null) {
						outDone = true;
					}
					else {
						stdOutBuff.append(line);
					}
				}
				
				// monitoring the standard error from the application
				boolean errReady = true;
				if(errReady) {
					line = brErr.readLine();
					if(line == null) {
						errDone = true;
					}
					else {
						stdErrBuff.append(line);
					}
				}
				
				// if standard out and standard error are not ready, wait for 250ms 
				// and try again to monitor the streams
				if(!outReady && !errReady)  {
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// NOP
					}
				}
			}	
			
			// we now have the output as an object from the run of the black-box jar
			// this output object contains both the standard output and the standard error
			output = new Output(stdOutBuff.toString(), stdErrBuff.toString());
			
		} catch (IOException e) {
			System.out.println("ERROR: IOException has prevented execution of the command: " + command); 
		}
		
		return output;
	}
	
	
	/**
	 * Method used to print the basic test output (std out/err)
	 * @param output - Output object containing std out/err to print 
	 */
	private void printBasicTestOutput(Output output) {
		System.out.println("stdout of execution: " + output.getStdOutString());
		System.out.println("stderr of execution: " + output.getStdErrString());
	}
	
	
	/**
	 * Method used to print raw code coverage stats including hits/probes
	 * @throws IOException
	 */
	private void printRawCoverageStats()  {
		System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
		System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(
							info.getStartTimeStamp()),
							new Date(info.getDumpTimeStamp()));
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					System.out.printf("%016x  %3d of %3d   %s%n",
							Long.valueOf(data.getId()),
							Integer.valueOf(getHitCount(data.getProbes())),
							Integer.valueOf(data.getProbes().length),
							data.getName());
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			System.out.println("Unable to display raw coverage stats due to IOException related to " + this.jacocoOutputFilePath);
		}
		System.out.println();
	}

	
	/**
	 * Method used to get hit count from the code coverage metrics
	 * @param data - boolean array of coverage data where true indicates hits
	 * @return int representation of count of total hits from supplied data
	 */
	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * Method for generating code coverage metrics including instructions, branches, lines, 
	 * methods and complexity. 
	 * 
	 * @return double representation of the percentage of code covered during testing
	 */
	private double generateSummaryCodeCoverageResults() {
		double percentCovered = 0.0;
		long total = 0;
		long covered = 0;
		try {
			// creating a new file for output in the jacoco output directory (one of the application arguments)
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);
			
			// use CoverageBuilder and Analyzer to assess code coverage from jacoco output file
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(
					execFileLoader.getExecutionDataStore(), coverageBuilder);
			
			// analyzeAll is the way to go to analyze all classes inside a container (jar or zip or directory)
			analyzer.analyzeAll(new File(this.jarToTestPath));
			
			
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				
				// report code coverage from all classes that are not the TestBounds class within the jar
				if(cc.getName().endsWith("TestBounds") == false) {
					total += cc.getInstructionCounter().getTotalCount();
					total += cc.getBranchCounter().getTotalCount();
					total += cc.getLineCounter().getTotalCount();
					total += cc.getMethodCounter().getTotalCount();
					total += cc.getComplexityCounter().getTotalCount();
						
					covered += cc.getInstructionCounter().getCoveredCount();
					covered += cc.getBranchCounter().getCoveredCount();
					covered += cc.getLineCounter().getCoveredCount();
					covered += cc.getMethodCounter().getCoveredCount();
					covered += cc.getComplexityCounter().getCoveredCount();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		percentCovered = ((double)covered / (double)total) * 100.0;
		return percentCovered;
	}
	
	
	/**
	 * This method shows an example of how to generate code coverage metrics from Jacoco
	 * 
	 * @return String representing code coverage results
	 */
	private String generateDetailedCodeCoverageResults() {
		String executionResults = "";
		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);
			
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(
					execFileLoader.getExecutionDataStore(), coverageBuilder);
			
			analyzer.analyzeAll(new File(this.jarToTestPath));
			
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				executionResults += "Coverage of class " + cc.getName() + ":\n";
				executionResults += getMetricResultString("instructions", cc.getInstructionCounter());
				executionResults += getMetricResultString("branches", cc.getBranchCounter());
				executionResults += getMetricResultString("lines", cc.getLineCounter());
				executionResults += getMetricResultString("methods", cc.getMethodCounter());
				executionResults += getMetricResultString("complexity", cc.getComplexityCounter());
				
				// adding this to a string is a little impractical with the size of some of the files, 
				// so we are commenting it out, but it shows that you can get the coverage status of each line
				// if you wanted to add debug argument to display this level of detail at command line level.... 
				//
				//for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				//	executionResults += "Line " + Integer.valueOf(i) + ": " + getStatusString(cc.getLine(i).getStatus()) + "\n";
				//}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return executionResults;
	}
	
	
	/**
	 * Method to translate the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status - integer representation of line coverage status provided by Jacoco
	 * @return String representation of line coverage status (not covered, partially covered, fully covered)
	 */
	@SuppressWarnings("unused")
	private String getStatusString(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "not covered";
		case ICounter.PARTLY_COVERED:
			return "partially covered";
		case ICounter.FULLY_COVERED:
			return "fully covered";
		}
		return "";
	}
	
	
	/**
	 * Method to translate the counter data and units into a human readable metric result String
	 * 
	 * @param unit
	 * @param counter
	 * @return
	 */
	private String getMetricResultString(final String unit, final ICounter counter) {
		final Integer missedCount = Integer.valueOf(counter.getMissedCount());
		final Integer totalCount = Integer.valueOf(counter.getTotalCount());
		return missedCount.toString() + " of " + totalCount.toString() + " " + unit + " missed\n";
	}

	
	/**
	 * This method is not meant to be part of the final framework. It was included to demonstrate
	 * three different ways to tap into the code coverage results/metrics using jacoco. 
	 * 
	 * This method is deprecated and will be removed from the final product after your team completes 
	 * development. Please do not add additional dependencies to this method. 
	 */
	@Deprecated 
	private void showCodeCoverageResultsExample() {
		
		// Below is the first example of how to tap into code coverage metrics
		double result = generateSummaryCodeCoverageResults();
		System.out.println("\n");
		System.out.println("percent covered: " + result);
		
		// Below is the second example of how to tap into code coverage metrics 
		System.out.println("\n");
		printRawCoverageStats();
		
		// Below is the third example of how to tap into code coverage metrics
		System.out.println("\n");
		System.out.println(generateDetailedCodeCoverageResults());
	}
	
	
}
