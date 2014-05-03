package org.cmdbuild.data.store;

import java.util.List;

public abstract class ForwardingStore<T extends Storable> implements Store<T> {

	private final Store<T> delegate;

	protected ForwardingStore(final Store<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Storable create(final T storable) {
		return delegate.create(storable);
	}

	@Override
	public T read(final Storable storable) {
		return delegate.read(storable);
	}

	@Override
	public void update(final T storable) {
		delegate.update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		delegate.delete(storable);

	}

	@Override
	public List<T> list() {
		return delegate.list();
	}

	@Override
	public List<T> list(final Groupable groupable) {
		return delegate.list(groupable);
	}

}
