package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public abstract class ForwardingQuerySpecs implements QuerySpecs {

	private final QuerySpecs delegate;

	protected ForwardingQuerySpecs(final QuerySpecs delegate) {
		this.delegate = delegate;
	}

	@Override
	public FromClause getFromClause() {
		return delegate.getFromClause();
	}

	@Override
	public List<JoinClause> getJoins() {
		return delegate.getJoins();
	}

	@Override
	public List<DirectJoinClause> getDirectJoins() {
		return delegate.getDirectJoins();
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		return delegate.getOrderByClauses();
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		return delegate.getAttributes();
	}

	@Override
	public WhereClause getWhereClause() {
		return delegate.getWhereClause();
	}

	@Override
	public Long getOffset() {
		return delegate.getOffset();
	}

	@Override
	public Long getLimit() {
		return delegate.getLimit();
	}

	@Override
	public boolean distinct() {
		return delegate.distinct();
	}

	@Override
	public boolean numbered() {
		return delegate.numbered();
	}

	@Override
	public WhereClause getConditionOnNumberedQuery() {
		return delegate.getConditionOnNumberedQuery();
	}

	@Override
	public boolean count() {
		return delegate.count();
	}

}
