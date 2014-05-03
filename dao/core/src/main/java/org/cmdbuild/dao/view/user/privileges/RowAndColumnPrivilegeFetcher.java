package org.cmdbuild.dao.view.user.privileges;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface RowAndColumnPrivilegeFetcher {

	/**
	 * Returns all {@link WhereClause}s related to the specified
	 * {@link CMEntryType}.
	 * 
	 * @param entryType
	 * 
	 * @return all {@link WhereClause}s related to the specified
	 *         {@link CMEntryType}.
	 */
	Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(CMEntryType entryType);

	/**
	 * Returns all {@link WhereClause}s related to the specified
	 * {@link CMEntryType}. All {@link WhereClause}s are referred to the second
	 * entry type. This is useful when we are looking at the privileges of a
	 * superclass but we want them referred to a specific subclass.
	 * 
	 * @param entryType
	 * @param entryTypeForClauses
	 * 
	 * @return all {@link WhereClause}s related to the specified
	 *         {@link CMEntryType}.
	 */
	Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(CMEntryType entryType, CMEntryType entryTypeForClauses);

	/**
	 * This method fetches column privileges for the currently logged user.
	 * 
	 * @param entryType
	 * @return
	 */
	Map<String, String> fetchAttributesPrivilegesFor(CMEntryType entryType);

}
