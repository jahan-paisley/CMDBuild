package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

public abstract class ForwardingValueSet implements CMValueSet {

	private final CMValueSet delegate;

	protected ForwardingValueSet(final CMValueSet delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object get(final String key) {
		return delegate.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return delegate.get(key, requiredType);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return delegate.getValues();
	}

}
