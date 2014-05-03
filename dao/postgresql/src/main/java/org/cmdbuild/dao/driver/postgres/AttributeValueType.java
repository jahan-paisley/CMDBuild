package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

/**
 * A simple DTO that contains the value and type
 */
class AttributeValueType {

	/**
	 * unquoted attribute name
	 */
	private final String name;
	private final Object value;
	private final CMAttributeType<?> type;

	AttributeValueType(final String name, final Object value, final CMAttributeType<?> type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	String getName() {
		return name;
	}

	Object getValue() {
		return value;
	}

	CMAttributeType<?> getType() {
		return type;
	}

}
