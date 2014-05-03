package org.cmdbuild.dao.query.clause.where;

public class EndsWithOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private EndsWithOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue endsWith(final Object value) {
		return new EndsWithOperatorAndValue(value);
	}

}
