package org.cmdbuild.dao.query.clause.from;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

public abstract class ForwardingFromClause implements FromClause {

	private final FromClause delegate;

	protected ForwardingFromClause(final FromClause delegate) {
		this.delegate = delegate;
	}

	@Override
	public CMEntryType getType() {
		return delegate.getType();
	}

	@Override
	public Alias getAlias() {
		return delegate.getAlias();
	}

	@Override
	public boolean isHistory() {
		return delegate.isHistory();
	}

	@Override
	public EntryTypeStatus getStatus(final CMEntryType entryType) {
		return delegate.getStatus(entryType);
	}

}
