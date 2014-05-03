package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class EmptyQuerySpecs implements QuerySpecs {

	@Override
	public FromClause getFromClause() {
		// don't change it
		return null;
	}

	@Override
	public List<JoinClause> getJoins() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DirectJoinClause> getDirectJoins() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WhereClause getWhereClause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean distinct() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean numbered() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WhereClause getConditionOnNumberedQuery() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean count() {
		throw new UnsupportedOperationException();
	}

}
