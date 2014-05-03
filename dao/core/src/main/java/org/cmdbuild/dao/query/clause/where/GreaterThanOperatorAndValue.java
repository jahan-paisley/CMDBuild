package org.cmdbuild.dao.query.clause.where;

public class GreaterThanOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private GreaterThanOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue gt(final Object value) {
		return new GreaterThanOperatorAndValue(value);
	}

}
