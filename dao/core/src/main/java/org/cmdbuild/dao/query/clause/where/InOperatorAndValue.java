package org.cmdbuild.dao.query.clause.where;

import java.util.List;

import com.google.common.collect.Lists;

public class InOperatorAndValue implements OperatorAndValue {

	private final List<Object> values;

	private InOperatorAndValue(final Object... objects) {
		this.values = Lists.newArrayList(objects);
	}

	public List<Object> getValue() {
		return values;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue in(final Object... objects) {
		return new InOperatorAndValue(objects);
	}

}
