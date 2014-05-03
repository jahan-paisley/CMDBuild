package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMClass;

public abstract class ForwardingCard extends ForwardingEntry implements CMCard {

	private final CMCard delegate;

	protected ForwardingCard(final CMCard delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public CMClass getType() {
		return delegate.getType();
	}

	@Override
	public Object getCode() {
		return delegate.getCode();
	}

	@Override
	public Object getDescription() {
		return delegate.getDescription();
	}

}
