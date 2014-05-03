package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang3.Validate;

public class StringAttributeType extends AbstractTextAttributeType {

	public final Integer length;

	public StringAttributeType() {
		this.length = null;
	}

	public StringAttributeType(final Integer length) {
		Validate.isTrue(length > 0);
		this.length = length;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean stringLimitExceeded(final String stringValue) {
		return (length != null && stringValue.length() > length);
	}

}
