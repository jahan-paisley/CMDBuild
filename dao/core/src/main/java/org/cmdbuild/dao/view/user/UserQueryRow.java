package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class UserQueryRow implements CMQueryRow {

	private final UserDataView view;
	private final CMQueryRow inner;

	static UserQueryRow newInstance(final UserDataView view, final CMQueryRow inner) {
		return new UserQueryRow(view, inner);
	}

	private UserQueryRow(final UserDataView view, final CMQueryRow inner) {
		this.view = view;
		this.inner = inner;
	}

	@Override
	public Long getNumber() {
		return inner.getNumber();
	}

	@Override
	public CMValueSet getValueSet(final Alias alias) {
		return inner.getValueSet(alias);
	}

	@Override
	public CMCard getCard(final Alias alias) {
		return proxy(inner.getCard(alias));
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return proxy(inner.getCard(type));
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		return inner.getRelation(alias);
	}

	@Override
	public QueryRelation getRelation(final CMDomain type) {
		return inner.getRelation(type);
	}

	private CMCard proxy(final CMCard card) {
		return UserCard.newInstance(view, card);
	}

}
