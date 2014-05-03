package org.cmdbuild.dao.entry;

import static java.lang.String.format;

import org.cmdbuild.dao.logging.LoggingSupport;
import org.slf4j.Logger;

/**
 * This card returns {@code null} instead of throwing an exception when
 * {@link #get(String)}and {@link #get(String, Class)} fails.
 */
public class NullOnErrorOfGetCard extends ForwardingCard {

	private static final Logger logger = LoggingSupport.logger;

	public static NullOnErrorOfGetCard of(final CMCard card) {
		return new NullOnErrorOfGetCard(card);
	}

	private NullOnErrorOfGetCard(final CMCard inner) {
		super(inner);
	}

	/**
	 * @return the value of the attribute, {@code null} if an error occurs.
	 */
	@Override
	public Object get(final String key) {
		try {
			return super.get(key);
		} catch (final Throwable e) {
			logger.warn(format("error getting attribute '%s'", key), e);
			return null;
		}
	}

	/**
	 * @return the value of the attribute, {@code null} if an error occurs.
	 */
	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		try {
			return super.get(key, requiredType);
		} catch (final Throwable e) {
			logger.warn(format("error getting attribute '%s'", key), e);
			return null;
		}
	}

}
