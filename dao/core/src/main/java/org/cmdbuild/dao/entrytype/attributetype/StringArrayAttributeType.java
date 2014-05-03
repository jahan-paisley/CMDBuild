package org.cmdbuild.dao.entrytype.attributetype;

public class StringArrayAttributeType extends AbstractAttributeType<String[]> {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String[] convertNotNullValue(final Object value) {
		if (!(value instanceof String[])) {
			throw illegalValue(value);
		}
		return (String[]) value;
	}

}
