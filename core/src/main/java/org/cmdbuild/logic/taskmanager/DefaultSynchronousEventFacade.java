package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.services.event.ForwardingObserver;
import org.cmdbuild.services.event.Observer;
import org.cmdbuild.services.event.ObserverCollector;
import org.cmdbuild.services.event.ObserverCollector.IdentifiableObserver;

public class DefaultSynchronousEventFacade implements SynchronousEventFacade {

	private static class DefaultIdentifiableObserver extends ForwardingObserver implements IdentifiableObserver {

		private final SynchronousEventTask task;

		public DefaultIdentifiableObserver(final SynchronousEventTask task, final Observer delegate) {
			super(delegate);
			this.task = task;
		}

		@Override
		public String getIdentifier() {
			return task.getId().toString();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("identifier", getIdentifier()) //
					.toString();
		}

	}

	private static Observer ALL_UNSUPPORTED = UnsupportedProxyFactory.of(Observer.class).create();

	private final ObserverCollector observerCollector;
	private final LogicAndObserverConverter converter;

	public DefaultSynchronousEventFacade(final ObserverCollector observerCollector,
			final LogicAndObserverConverter converter) {
		this.observerCollector = observerCollector;
		this.converter = converter;
	}

	@Override
	public void create(final SynchronousEventTask task) {
		Validate.notNull(task.getId(), "missing id");
		if (task.isActive()) {
			final Observer observer = converter.from(task).toObserver();
			observerCollector.add(new DefaultIdentifiableObserver(task, observer));
		}
	}

	@Override
	public void delete(final SynchronousEventTask task) {
		Validate.notNull(task.getId(), "missing id");
		if (task.isActive()) {
			observerCollector.remove(new DefaultIdentifiableObserver(task, ALL_UNSUPPORTED));
		}
	}

}
