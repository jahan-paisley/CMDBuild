package org.cmdbuild.workflow.event;

import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;

public class NullUpdateOperationListener implements UpdateOperationListener {

	@Override
	public void processInstanceStarted(final int sessionId) {
	}

	@Override
	public void processInstanceAborted(final int sessionId) {
	}

	@Override
	public void processInstanceSuspended(final int sessionId) {
	}

	@Override
	public void processInstanceResumed(final int sessionId) {
	}

	@Override
	public void activityInstanceAborted(final int sessionId) {
	}

	@Override
	public void activityInstanceAdvanced(final int sessionId) {
	}

	@Override
	public void abortedOperation(final int sessionId) {
	}

}
