package org.cmdbuild.dao.entrytype.attributetype;

public class UndefinedAttributeType implements CMAttributeType<Object> {

	private static final UndefinedAttributeType INSTANCE = new UndefinedAttributeType();

	public static final UndefinedAttributeType undefined() {
		return INSTANCE;
	}

	private UndefinedAttributeType() {
		// prevents instantiation
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Object convertValue(final Object value) {
		return value;
	}
}
