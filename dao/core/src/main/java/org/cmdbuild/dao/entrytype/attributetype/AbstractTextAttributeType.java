package org.cmdbuild.dao.entrytype.attributetype;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractTextAttributeType extends AbstractAttributeType<String> {

	@Override
	protected String convertNotNullValue(final Object value) {
		if (!(value instanceof String)) {
			throw new IllegalArgumentException();
		}
		final String stringValue = (String) value;
		if (isBlank(stringValue)) {
			return null;
		}
		if (stringLimitExceeded(stringValue)) {
			throw illegalValue(value);
		}
		return stringValue;
	}

	protected boolean stringLimitExceeded(final String stringValue) {
		return false;
	}

}
