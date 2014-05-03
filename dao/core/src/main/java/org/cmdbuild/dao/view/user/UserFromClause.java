package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.query.clause.from.ClassFromClause;
import org.cmdbuild.dao.query.clause.from.ForwardingFromClause;
import org.cmdbuild.dao.query.clause.from.FromClause;

public class UserFromClause extends ForwardingFromClause {

	static FromClause newInstance(final UserDataView userDataView, final FromClause delegate) {
		return new UserFromClause(userDataView, delegate);
	}

	private final UserDataView userDataView;
	private final FromClause delegate;

	private UserFromClause(final UserDataView userDataView, final FromClause delegate) {
		super(delegate);
		this.userDataView = userDataView;
		this.delegate = delegate;
	}

	@Override
	public CMEntryType getType() {
		return userDataView.proxy(super.getType());
	}

	@Override
	public EntryTypeStatus getStatus(final CMEntryType entryType) {
		return new NullEntryTypeVisitor() {

			private EntryTypeStatus status;

			public EntryTypeStatus getStatus(final CMEntryType entryType) {
				status = UserFromClause.super.getStatus(entryType);
				entryType.accept(this);
				return status;
			}

			@Override
			public void visit(final CMClass type) {
				final ClassFromClause classFromClause = new ClassFromClause(userDataView, delegate.getType(),
						delegate.getAlias());
				status = classFromClause.getStatus(entryType);
			}

		}.getStatus(entryType);
	}

}
