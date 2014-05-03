package org.cmdbuild.dao.query.clause.where;

public class EmptyArrayOperatorAndValue implements OperatorAndValue {

	private EmptyArrayOperatorAndValue() {
	}

	public static EmptyArrayOperatorAndValue emptyArray() {
		return new EmptyArrayOperatorAndValue();
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

}
