package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface QuerySpecs {

	FromClause getFromClause();

	List<JoinClause> getJoins();

	List<DirectJoinClause> getDirectJoins();

	List<OrderByClause> getOrderByClauses();

	Iterable<QueryAliasAttribute> getAttributes();

	WhereClause getWhereClause();

	Long getOffset();

	Long getLimit();

	boolean distinct();

	boolean numbered();

	WhereClause getConditionOnNumberedQuery();

	boolean count();

}
