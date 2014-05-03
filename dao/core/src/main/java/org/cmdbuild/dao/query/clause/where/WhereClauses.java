package org.cmdbuild.dao.query.clause.where;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;

public class WhereClauses {

	private WhereClauses() {
		// prevents instantiation
	}

	public static FunctionWhereClause function(final QueryAliasAttribute attribute, final String name,
			final Long userId, final Long roleId, final CMEntryType entryType) {
		return new FunctionWhereClause(attribute, name, userId, roleId, entryType);
	}

}
