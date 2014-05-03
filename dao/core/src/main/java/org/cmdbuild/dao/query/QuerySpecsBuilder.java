package org.cmdbuild.dao.query;

import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/**
 * Builder for {@link QuerySpecs}.
 */
public interface QuerySpecsBuilder extends Builder<QuerySpecs> {

	QuerySpecsBuilder select(Object... attrDef);

	QuerySpecsBuilder distinct();

	QuerySpecsBuilder _from(CMEntryType entryType, Alias alias);

	QuerySpecsBuilder from(CMEntryType fromEntryType, Alias fromAlias);

	QuerySpecsBuilder from(CMClass cmClass);

	/*
	 * TODO: Consider more join levels (join with join tables)
	 */
	QuerySpecsBuilder join(CMClass joinClass, Over overClause);

	QuerySpecsBuilder join(CMClass joinClass, Alias joinClassAlias, Over overClause);

	QuerySpecsBuilder join(CMClass joinClass, Alias joinClassAlias, Over overClause, Source source);

	/*
	 * TODO refactor to have a single join method
	 */
	QuerySpecsBuilder leftJoin(CMClass joinClass, Alias joinClassAlias, Over overClause);

	QuerySpecsBuilder leftJoin(CMClass joinClass, Alias joinClassAlias, Over overClause, Source source);

	QuerySpecsBuilder where(WhereClause clause);

	QuerySpecsBuilder offset(Number offset);

	QuerySpecsBuilder limit(Number limit);

	QuerySpecsBuilder orderBy(Object attribute, Direction direction);

	QuerySpecsBuilder numbered();

	QuerySpecsBuilder numbered(WhereClause whereClause);

	QuerySpecsBuilder count();

	CMQueryResult run();

}