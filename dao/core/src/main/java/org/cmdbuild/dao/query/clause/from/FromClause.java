package org.cmdbuild.dao.query.clause.from;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

public interface FromClause {

	interface EntryTypeStatus {

		boolean isAccessible();

		boolean isActive();

	}

	CMEntryType getType();

	Alias getAlias();

	boolean isHistory();

	EntryTypeStatus getStatus(CMEntryType entryType);

}
