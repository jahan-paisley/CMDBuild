package org.cmdbuild.dao.query;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;

/**
 * Immutable interface to mask result object building.
 */
public interface CMQueryRow {

	/**
	 * Gets the row number.
	 * 
	 * @return the row number if {@link QuerySpecs#numbered()} has been
	 *         specified, {@code null} otherwise.
	 */
	Long getNumber();

	CMValueSet getValueSet(Alias alias);

	CMCard getCard(Alias alias);

	CMCard getCard(CMClass type);

	QueryRelation getRelation(Alias alias);

	QueryRelation getRelation(CMDomain type);

}
