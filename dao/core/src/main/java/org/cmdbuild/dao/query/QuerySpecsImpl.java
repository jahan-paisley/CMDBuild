package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.Lists;

public class QuerySpecsImpl implements QuerySpecs {

	public static class Builder implements org.cmdbuild.common.Builder<QuerySpecsImpl> {

		private FromClause fromClause;
		private boolean distinct;
		private boolean numbered;
		private WhereClause conditionOnNumberedQuery;
		private boolean count;

		public Builder() {
			conditionOnNumberedQuery = EmptyWhereClause.emptyWhereClause();
		}

		@Override
		public QuerySpecsImpl build() {
			return new QuerySpecsImpl(this);
		};

		public Builder fromClause(final FromClause fromClause) {
			this.fromClause = fromClause;
			return this;
		}

		public Builder distinct(final boolean distinct) {
			this.distinct = distinct;
			return this;
		}

		public Builder numbered(final boolean numbered) {
			this.numbered = numbered;
			return this;
		}

		public Builder conditionOnNumberedQuery(final WhereClause whereClause) {
			this.conditionOnNumberedQuery = whereClause;
			return this;
		}

		public Builder count(final boolean count) {
			this.count = count;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final FromClause fromClause;
	private final List<JoinClause> joinClauses;
	private final List<DirectJoinClause> directJoinClauses;
	private final List<QueryAliasAttribute> attributes;
	private final List<OrderByClause> orderByClauses;
	private Long offset;
	private Long limit;
	private WhereClause whereClause;
	private final boolean distinct;
	private final boolean numbered;
	private final WhereClause conditionOnNumberedQuery;
	private final boolean count;

	private QuerySpecsImpl(final Builder builder) {
		this.fromClause = builder.fromClause;
		this.distinct = builder.distinct;
		this.numbered = builder.numbered;
		this.conditionOnNumberedQuery = builder.conditionOnNumberedQuery;
		this.count = builder.count;

		this.joinClauses = Lists.newArrayList();
		this.directJoinClauses = Lists.newArrayList();
		this.attributes = Lists.newArrayList();
		this.orderByClauses = Lists.newArrayList();
		this.offset = null;
		this.limit = null;
		this.whereClause = EmptyWhereClause.emptyWhereClause();
	}

	@Override
	public FromClause getFromClause() {
		return fromClause;
	}

	public void addJoin(final JoinClause joinClause) {
		joinClauses.add(joinClause);
	}

	public void addDirectJoin(final DirectJoinClause directJoinClause) {
		directJoinClauses.add(directJoinClause);
	}

	@Override
	public List<JoinClause> getJoins() {
		return joinClauses;
	}

	@Override
	public List<DirectJoinClause> getDirectJoins() {
		return directJoinClauses;
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		return this.attributes;
	}

	public void addOrderByClause(final OrderByClause orderByClause) {
		this.orderByClauses.add(orderByClause);
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		return orderByClauses;
	}

	public void addSelectAttribute(final QueryAliasAttribute attribute) {
		attributes.add(attribute);
	}

	public void setWhereClause(final WhereClause whereClause) {
		this.whereClause = whereClause;
	}

	@Override
	public WhereClause getWhereClause() {
		return whereClause;
	}

	public void setOffset(final Long offset) {
		this.offset = offset;
	}

	@Override
	public Long getOffset() {
		return offset;
	}

	public void setLimit(final Long limit) {
		this.limit = limit;
	}

	@Override
	public Long getLimit() {
		return limit;
	}

	@Override
	public boolean distinct() {
		return distinct;
	}

	@Override
	public boolean numbered() {
		return numbered;
	}

	@Override
	public WhereClause getConditionOnNumberedQuery() {
		return conditionOnNumberedQuery;
	}

	@Override
	public boolean count() {
		return count;
	}

}
