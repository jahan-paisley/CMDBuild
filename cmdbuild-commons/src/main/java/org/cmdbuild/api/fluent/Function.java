package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class Function {

	private final String functionName;
	private final Map<String, Object> inputParameters;

	public Function(final String functionName) {
		this.functionName = functionName;
		inputParameters = new HashMap<String, Object>();
	}

	public String getFunctionName() {
		return functionName;
	}

	public Map<String, Object> getInputs() {
		return unmodifiableMap(inputParameters);
	}

	void set(final String name, final Object value) {
		inputParameters.put(name, value);
	}

}