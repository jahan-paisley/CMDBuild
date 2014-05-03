package org.cmdbuild.logic.scheduler;

public abstract class ForwardingSchedulerLogic implements SchedulerLogic {

	private final SchedulerLogic inner;

	protected ForwardingSchedulerLogic(final SchedulerLogic inner) {
		this.inner = inner;
	}

	@Override
	public void startScheduler() {
		inner.startScheduler();
	}

	@Override
	public void stopScheduler() {
		inner.stopScheduler();
	}

}
