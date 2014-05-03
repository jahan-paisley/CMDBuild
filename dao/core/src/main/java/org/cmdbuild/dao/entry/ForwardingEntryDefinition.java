package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entry.CMEntry.CMEntryDefinition;

public abstract class ForwardingEntryDefinition implements CMEntryDefinition {

	private final CMEntryDefinition delegate;

	protected ForwardingEntryDefinition(final CMEntryDefinition delegate) {
		this.delegate = delegate;
	}

	@Override
	public CMEntryDefinition set(final String key, final Object value) {
		delegate.set(key, value);
		return this;
	}

	@Override
	public CMEntryDefinition setUser(final String user) {
		delegate.setUser(user);
		return this;
	}

	@Override
	public CMEntry save() {
		return delegate.save();
	}

}
