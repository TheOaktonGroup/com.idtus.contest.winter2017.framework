package testertypecheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesterTypeCheckTestBounds {

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> testBounds() {
        Map returnMap = new HashMap();
        
        Map dependentParamsMap = new HashMap();
        
        Map dependentTypeMap = new HashMap();
        dependentTypeMap.put("type", Integer.class);
        dependentParamsMap.put("\\s*--Integer\\s*", dependentTypeMap);
        
        dependentTypeMap = new HashMap();
        dependentTypeMap.put("type", Double.class);
        dependentParamsMap.put("\\s*--Double\\s*", dependentTypeMap);
        
        dependentTypeMap = new HashMap();
        dependentTypeMap.put("type", String.class);
        dependentParamsMap.put("\\s*--String\\s*", dependentTypeMap);
        
        dependentTypeMap = new HashMap();
        dependentTypeMap.put("type", String.class);
        dependentTypeMap.put("format", "a_fixed_string:<<REPLACE_ME_INT>>:<<REPLACE_ME_STRING>>");
        dependentParamsMap.put("\\s*--NextOneIsFormatted\\s*", dependentTypeMap);
        
        dependentTypeMap = new HashMap();
        dependentTypeMap.put("type", String.class);
        List<String> enumeratedValuesList = new ArrayList<String>();
        enumeratedValuesList.add("--Integer");
        enumeratedValuesList.add("--Double");
        enumeratedValuesList.add("--String");
        enumeratedValuesList.add("--NextOneIsFormatted");
        enumeratedValuesList.add("--FormattedStringWithInt=<<REPLACE_ME_INT>>");
        enumeratedValuesList.add("--FormattedStringWithString=<<REPLACE_ME_STRING>>");
        dependentTypeMap.put("enumerated values", enumeratedValuesList);
        dependentParamsMap.put("", dependentTypeMap);
        
        returnMap.put("dependent parameters", dependentParamsMap);
        
        
        
        
        List testList = new ArrayList();
        
        Map test = new HashMap();
        List testParameterList = new ArrayList();
        testParameterList.add("--Integer");
        testParameterList.add(1);
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);
        
        
        test = new HashMap();
        testParameterList = new ArrayList();
        testParameterList.add("--Double");
        testParameterList.add(20.0);
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);


        test = new HashMap();
        testParameterList = new ArrayList();
        testParameterList.add("--String");
        testParameterList.add("\"some string\"");
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);

        
        test = new HashMap();
        testParameterList = new ArrayList();
        testParameterList.add("--NextOneIsFormatted");
        testParameterList.add("\"a_fixed_string:1234:some string\"");
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);

        
        test = new HashMap();
        testParameterList = new ArrayList();
        testParameterList.add("--FormattedStringWithInt=1234");
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);

        
        test = new HashMap();
        testParameterList = new ArrayList();
        testParameterList.add("\"--FormattedStringWithString=some string\"");
        test.put("parameters", testParameterList);
        test.put("stdOutExpectedResultRegex", ".*read successfully.$");
        test.put("stdErrExpectedResultRegex", "");
        testList.add(test);
        
        returnMap.put("tests", testList);

        
        return returnMap;
    }
}
