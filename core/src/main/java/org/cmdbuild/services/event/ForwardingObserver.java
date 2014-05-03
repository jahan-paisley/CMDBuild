package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;

public abstract class ForwardingObserver implements Observer {

	private final Observer delegate;

	protected ForwardingObserver(final Observer delegate) {
		this.delegate = delegate;
	}

	@Override
	public void afterCreate(final CMCard current) {
		delegate.afterCreate(current);
	}

	@Override
	public void beforeUpdate(final CMCard current, final CMCard next) {
		delegate.beforeUpdate(current, next);
	}

	@Override
	public void afterUpdate(final CMCard previous, final CMCard current) {
		delegate.afterUpdate(previous, current);
	}

	@Override
	public void beforeDelete(final CMCard current) {
		delegate.beforeDelete(current);
	}

}