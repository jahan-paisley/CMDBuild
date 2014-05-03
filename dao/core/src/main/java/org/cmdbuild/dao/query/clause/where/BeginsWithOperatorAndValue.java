package org.cmdbuild.dao.query.clause.where;

public class BeginsWithOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private BeginsWithOperatorAndValue(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue beginsWith(final Object value) {
		return new BeginsWithOperatorAndValue(value);
	}

}
