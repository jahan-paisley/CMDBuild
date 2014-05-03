package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;

public abstract class ForwardingCardDefinition extends ForwardingEntryDefinition implements CMCardDefinition {

	private final CMCardDefinition delegate;

	protected ForwardingCardDefinition(final CMCardDefinition delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public CMCardDefinition set(final String key, final Object value) {
		delegate.set(key, value);
		return this;
	}

	@Override
	public CMCardDefinition setCode(final Object value) {
		delegate.setCode(value);
		return this;
	}

	@Override
	public CMCardDefinition setDescription(final Object value) {
		delegate.setDescription(value);
		return this;
	}

	@Override
	public CMCard save() {
		return delegate.save();
	}

}
