package org.cmdbuild.dao.entry;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

/**
 * Immutable data store entry.
 */
public interface CMEntry extends CMValueSet {

	/**
	 * {@link CMEntry} mutator object.
	 */
	interface CMEntryDefinition {

		CMEntryDefinition set(String key, Object value);

		CMEntryDefinition setUser(String user);

		// TODO check if this is really needed
		CMEntry save();

	}

	CMEntryType getType();

	Long getId();

	String getUser();

	DateTime getBeginDate();

	DateTime getEndDate();

	Iterable<Map.Entry<String, Object>> getAllValues();

}
