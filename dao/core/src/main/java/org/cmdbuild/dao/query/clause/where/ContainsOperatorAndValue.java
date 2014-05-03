package org.cmdbuild.dao.query.clause.where;

public class ContainsOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private ContainsOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue contains(final Object value) {
		return new ContainsOperatorAndValue(value);
	}

}
