package org.cmdbuild.dao.driver.postgres.query;

import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.from.FromClause;

public class FromPartCreator extends PartCreator {

	public FromPartCreator(final QuerySpecs querySpecs) {
		super();
		sb.append("FROM ");
		/*
		 * TODO check if this is really needed
		 * 
		 * if (query.getFromType().holdsHistory()) { sb.append("ONLY "); }
		 */
		sb.append(quoteType(fromClause(querySpecs).getType())).append(" AS ")
				.append(AliasQuoter.quote(fromClause(querySpecs).getAlias()));
	}

	private FromClause fromClause(final QuerySpecs querySpecs) {
		return querySpecs.getFromClause();
	}

}
