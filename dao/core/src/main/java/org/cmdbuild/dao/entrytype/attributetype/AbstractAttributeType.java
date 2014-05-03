package org.cmdbuild.dao.entrytype.attributetype;

import static java.lang.String.format;

import org.cmdbuild.dao.logging.LoggingSupport;
import org.slf4j.Logger;

public abstract class AbstractAttributeType<T> implements CMAttributeType<T> {

	protected static final Logger logger = LoggingSupport.logger;

	@Override
	public final T convertValue(final Object value) {
		if (value == null) {
			return null;
		} else {
			return convertNotNullValue(value);
		}
	}

	protected RuntimeException illegalValue(final Object value) {
		logger.error("invalid value '{}' for attribute type '{}'", value, getClass());
		return new IllegalArgumentException(format("invalid value '%s'", value));
	}

	/**
	 * Casts a value that is assumed not to be null to the native type.
	 * 
	 * @param value
	 *            not null value of any type.
	 * 
	 * @return the value of the native type.
	 */
	protected abstract T convertNotNullValue(Object value);

}
