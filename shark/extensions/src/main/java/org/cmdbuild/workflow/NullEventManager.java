package org.cmdbuild.workflow;

public class NullEventManager implements SimpleEventManager {

	@Override
	public void processStarted(final ProcessInstance processInstance) {
	}

	@Override
	public void processClosed(final ProcessInstance processInstance) {
	}

	@Override
	public void processSuspended(final ProcessInstance processInstance) {
	}

	@Override
	public void processResumed(final ProcessInstance processInstance) {
	}

	@Override
	public void activityStarted(final ActivityInstance activityInstance) {
	}

	@Override
	public void activityClosed(final ActivityInstance activityInstance) {
	}

}
