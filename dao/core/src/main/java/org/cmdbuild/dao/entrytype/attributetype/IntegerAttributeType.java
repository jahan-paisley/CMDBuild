package org.cmdbuild.dao.entrytype.attributetype;

public class IntegerAttributeType extends AbstractAttributeType<Integer> {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Integer convertNotNullValue(final Object value) {
		Integer intValue;
		if (value instanceof Integer) {
			intValue = (Integer) value;
		} else if (value instanceof Number) {
			intValue = ((Number) value).intValue();
		} else if (value instanceof String) {
			final String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				intValue = null;
			} else {
				// throws NumberFormatException
				intValue = Integer.valueOf(stringValue);
			}
		} else {
			throw illegalValue(value);
		}
		return intValue;
	}

}
