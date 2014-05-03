package org.cmdbuild.dao.query.clause.where;

public class EqualsOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private EqualsOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue eq(final Object value) {
		return new EqualsOperatorAndValue(value);
	}

}
