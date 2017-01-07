package testertypecheck;


public class TesterTypeCheck {
	
    static public void main(String[] argv) {
    	TesterTypeCheck testerTypeCheck = new TesterTypeCheck();
    	String paramTypeName = null;
    	if(argv.length > 0) {
    		paramTypeName = argv[0];
    	}
    	String parameter = null;
    	if(argv.length > 1) {
    		parameter = argv[1];
    	}
    	testerTypeCheck.checkParameter(paramTypeName, parameter);
    }

    private void checkParameter(String paramTypeName, String parameter) {

    	if(paramTypeName.equals("--Integer")) {
    		checkInteger(parameter);
    	}
    	else if(paramTypeName.equals("--Double")) {
    		checkDouble(parameter);
    	}
    	else if(paramTypeName.equals("--String")) {
    		checkString(parameter);
    	}
    	else if(paramTypeName.equals("--NextOneIsFormatted")) {
    		checkFormattedString(parameter);
    	}
    	else if(paramTypeName.matches("--FormattedStringWithInt=-?[0-9]+")) {
    		checkFormattedStringWithInt(paramTypeName);
    	}
    	else if(paramTypeName.matches("--FormattedStringWithString=.+")) {
    		checkFormattedStringWithString(paramTypeName);
    	}
    	else {
			System.out.println("Parameter Type Name ("+paramTypeName+") was not recognized.");
    	}
    }
    
    private void checkInteger(String parameter) {
    	
    	try {
    		int input = Integer.parseInt(parameter);
			System.out.println("Integer parameter ("+input+") read successfully.");
    	} catch(NumberFormatException nfe) {
			System.out.println("Integer parameter ("+parameter+") not parseable.");
    	}
    }
    
    
    private void checkDouble(String parameter) {
    	
    	try {
    		double input = Double.parseDouble(parameter);
			System.out.println("Double parameter ("+input+") read successfully.");
    	} catch(NumberFormatException nfe) {
			System.out.println("Double parameter ("+parameter+") not parseable.");
    	}
    }
    
    private void checkString(String parameter) {
    	
    	System.out.println("String parameter ("+parameter+") read successfully.");
    }
    
    private void checkFormattedString(String parameter) {
    	
    	if(parameter.matches("a_fixed_string:[0-9]+:.+")) {
    		System.out.println("String parameter ("+parameter+") read successfully.");
    	}
    	else {
    		System.out.println("String parameter ("+parameter+") not parseable.");
    	}
    }
    
    private void checkFormattedStringWithInt(String parameter) {
    	
    	try {
    		int input = Integer.parseInt(parameter.split("=")[1]);
			System.out.println("Integer parameter ("+input+") read successfully.");
    	} catch(NumberFormatException nfe) {
			System.out.println("Integer parameter ("+parameter+") not parseable.");
    	}
    }
    
    private void checkFormattedStringWithString(String parameter) {
    	
    	System.out.println("Formatted String parameter ("+parameter+") read successfully.");
    }
    
}
