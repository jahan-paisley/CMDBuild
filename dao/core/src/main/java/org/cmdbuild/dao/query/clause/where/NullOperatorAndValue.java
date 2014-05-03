package org.cmdbuild.dao.query.clause.where;

public class NullOperatorAndValue implements OperatorAndValue {

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

	public static OperatorAndValue isNull() {
		return new NullOperatorAndValue();
	}

}
