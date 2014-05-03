package org.cmdbuild.dao.query;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public abstract class ForwardingQuerySpecsBuilder implements QuerySpecsBuilder {

	private final QuerySpecsBuilder delegate;

	protected ForwardingQuerySpecsBuilder(final QuerySpecsBuilder delegate) {
		this.delegate = delegate;
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return delegate.select(attrDef);
	}

	@Override
	public QuerySpecsBuilder distinct() {
		return delegate.distinct();
	}

	@Override
	public QuerySpecsBuilder _from(final CMEntryType entryType, final Alias alias) {
		return delegate._from(entryType, alias);
	}

	@Override
	public QuerySpecsBuilder from(final CMEntryType fromEntryType, final Alias fromAlias) {
		return delegate.from(fromEntryType, fromAlias);
	}

	@Override
	public QuerySpecsBuilder from(final CMClass cmClass) {
		return delegate.from(cmClass);
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Over overClause) {
		return delegate.join(joinClass, overClause);
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		return delegate.join(joinClass, joinClassAlias, overClause);
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		return delegate.join(joinClass, joinClassAlias, overClause, source);
	}

	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		return delegate.leftJoin(joinClass, joinClassAlias, overClause);
	}

	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		return delegate.leftJoin(joinClass, joinClassAlias, overClause, source);
	}

	@Override
	public QuerySpecsBuilder where(final WhereClause clause) {
		return delegate.where(clause);
	}

	@Override
	public QuerySpecsBuilder offset(final Number offset) {
		return delegate.offset(offset);
	}

	@Override
	public QuerySpecsBuilder limit(final Number limit) {
		return delegate.limit(limit);
	}

	@Override
	public QuerySpecsBuilder orderBy(final Object attribute, final Direction direction) {
		return delegate.orderBy(attribute, direction);
	}

	@Override
	public QuerySpecsBuilder numbered() {
		return delegate.numbered();
	}

	@Override
	public QuerySpecsBuilder numbered(final WhereClause whereClause) {
		return delegate.numbered(whereClause);
	}

	@Override
	public QuerySpecsBuilder count() {
		return delegate.count();
	}

	@Override
	public QuerySpecs build() {
		return delegate.build();
	}

	@Override
	public CMQueryResult run() {
		return delegate.run();
	}

}
