package org.cmdbuild.dao.query.clause.where;

public class LessThanOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private LessThanOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue lt(final Object value) {
		return new LessThanOperatorAndValue(value);
	}

}
