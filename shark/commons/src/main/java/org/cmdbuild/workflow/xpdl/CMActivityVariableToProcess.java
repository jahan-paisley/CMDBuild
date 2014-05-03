package org.cmdbuild.workflow.xpdl;

import org.apache.commons.lang3.Validate;

public class CMActivityVariableToProcess {

	public enum Type {
		READ_ONLY,
		READ_WRITE,
		READ_WRITE_REQUIRED
	}

	private final String name;
	private final Type type;

	public CMActivityVariableToProcess(final String name, final Type type) {
		Validate.notEmpty(name, "Variable names must be non-empty");
		Validate.notNull(type, "Variable type must be specified");
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}
}
