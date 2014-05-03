package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.select.FunctionSelect;

@SuppressWarnings("unchecked")
public class FunctionSelectImpl extends SelectElementImpl implements FunctionSelect {

	String name;
	String as;

	@Override
	public String getAs() {
		return as;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setAs(final String functionAs) {
		this.as = functionAs;
	}

	@Override
	public void setName(final String functionName) {
		this.name = functionName;
	}

}
