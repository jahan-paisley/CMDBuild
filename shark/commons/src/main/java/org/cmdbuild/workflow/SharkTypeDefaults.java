package org.cmdbuild.workflow;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

/**
 * Default values for Shark "native" types.
 * 
 * Methods are needed because new of mutable values {@link ReferenceType} and
 * {@link LookupType}.
 */
public final class SharkTypeDefaults {

	private SharkTypeDefaults() {
	}

	public static Boolean defaultBoolean() {
		return Boolean.FALSE;
	}

	public static Date defaultDate() {
		return null;
	}

	public static Double defaultDouble() {
		return 0.0;
	}

	public static ReferenceType defaultReference() {
		return new ReferenceType();
	}

	public static Long defaultInteger() {
		return 0L;
	}

	public static LookupType defaultLookup() {
		return new LookupType();
	}

	public static String defaultString() {
		return StringUtils.EMPTY;
	}

}