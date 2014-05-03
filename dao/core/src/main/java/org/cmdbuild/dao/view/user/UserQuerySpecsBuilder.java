package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/**
 * Used for build an instance of {@link UserQuerySpecs}. Adds user-specific
 * behavior to the build process.
 */
public class UserQuerySpecsBuilder implements QuerySpecsBuilder {

	static UserQuerySpecsBuilder newInstance(final QuerySpecsBuilder querySpecsBuilder, final UserDataView userDataView) {
		return new UserQuerySpecsBuilder(querySpecsBuilder, userDataView);
	}

	private final QuerySpecsBuilder delegate;
	private final UserDataView userDataView;

	private UserQuerySpecsBuilder(final QuerySpecsBuilder delegate, final UserDataView userDataView) {
		this.delegate = delegate;
		this.userDataView = userDataView;
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		delegate.select(attrDef);
		return this;
	}

	@Override
	public QuerySpecsBuilder distinct() {
		delegate.distinct();
		return this;
	}

	@Override
	public QuerySpecsBuilder _from(final CMEntryType entryType, final Alias alias) {
		delegate._from(entryType, alias);
		return this;
	}

	@Override
	public QuerySpecsBuilder from(final CMEntryType fromEntryType, final Alias fromAlias) {
		delegate.from(fromEntryType, fromAlias);
		return this;
	}

	@Override
	public QuerySpecsBuilder from(final CMClass cmClass) {
		delegate.from(cmClass);
		return this;
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Over overClause) {
		delegate.join(joinClass, overClause);
		return this;
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		delegate.join(joinClass, joinClassAlias, overClause);
		return this;
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		delegate.join(joinClass, joinClassAlias, overClause, source);
		return this;
	}

	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		delegate.leftJoin(joinClass, joinClassAlias, overClause);
		return this;
	}

	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		delegate.leftJoin(joinClass, joinClassAlias, overClause, source);
		return this;
	}

	@Override
	public QuerySpecsBuilder where(final WhereClause clause) {
		delegate.where(clause);
		return this;
	}

	@Override
	public QuerySpecsBuilder offset(final Number offset) {
		delegate.offset(offset);
		return this;
	}

	@Override
	public QuerySpecsBuilder limit(final Number limit) {
		delegate.limit(limit);
		return this;
	}

	@Override
	public QuerySpecsBuilder orderBy(final Object attribute, final Direction direction) {
		delegate.orderBy(attribute, direction);
		return this;
	}

	@Override
	public QuerySpecsBuilder numbered() {
		delegate.numbered();
		return this;
	}

	@Override
	public QuerySpecsBuilder numbered(final WhereClause whereClause) {
		delegate.numbered(whereClause);
		return this;
	}

	@Override
	public QuerySpecsBuilder count() {
		delegate.count();
		return this;
	}

	@Override
	public QuerySpecs build() {
		return userDataView.proxy(delegate.build());
	}

	@Override
	public CMQueryResult run() {
		return userDataView.executeQuery(build());
	}

}
