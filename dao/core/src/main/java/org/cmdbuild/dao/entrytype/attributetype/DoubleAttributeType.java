package org.cmdbuild.dao.entrytype.attributetype;

public class DoubleAttributeType extends AbstractAttributeType<Double> {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Double convertNotNullValue(final Object value) {
		Double doubleValue;
		if (value instanceof Double) {
			doubleValue = (Double) value;
		} else if (value instanceof String) {
			final String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				doubleValue = null;
			} else {
				// throws NumberFormatException
				doubleValue = Double.valueOf(stringValue);
			}
		} else {
			throw illegalValue(value);
		}
		return doubleValue;
	}

}
