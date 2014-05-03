package org.cmdbuild.dao.query.clause.where;

public class StringArrayOverlapOperatorAndValue implements OperatorAndValue {

	private final Object value;

	private StringArrayOverlapOperatorAndValue(final Object value) {
		if (null == value) {
			this.value = "";
		} else {
			this.value = value;
		}
	}

	public static StringArrayOverlapOperatorAndValue stringArrayOverlap(final Object value) {
		return new StringArrayOverlapOperatorAndValue(value);
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void accept(final OperatorAndValueVisitor visitor) {
		visitor.visit(this);
	}

}
