package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.select.FieldSelect;

public class FieldSelectImpl extends CQLElementImpl implements FieldSelect {

	String as;
	String name;

	@Override
	public String getAs() {
		return as;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setAs(final String attributeAs) {
		this.as = attributeAs;
	}

	@Override
	public void setName(final String attributeName) {
		this.name = attributeName;
	}

}
