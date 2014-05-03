package org.cmdbuild.services.soap.structure;

import java.util.List;

public class FunctionSchema {

	private String name;
	private List<AttributeSchema> input;
	private List<AttributeSchema> output;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<AttributeSchema> getInput() {
		return input;
	}

	public void setInput(final List<AttributeSchema> input) {
		this.input = input;
	}

	public List<AttributeSchema> getOutput() {
		return output;
	}

	public void setOutput(final List<AttributeSchema> output) {
		this.output = output;
	}

}
