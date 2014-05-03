package org.cmdbuild.dao.entrytype.attributetype;

public class CharAttributeType extends AbstractTextAttributeType {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean stringLimitExceeded(final String stringValue) {
		return (stringValue.length() > 1);
	}

	private static CMAttributeType<?> daoType = new CharAttributeType();

	protected CMAttributeType<?> getDaoType() {
		return daoType;
	}

}
