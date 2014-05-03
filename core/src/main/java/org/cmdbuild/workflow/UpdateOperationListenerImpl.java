package org.cmdbuild.workflow;

import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;

/**
 * This implementation is tightly bound to how the {@link DefaultWorkflowEngine}
 * works. If lets the engine save the newly created process instance after it
 * killed the activity instances that it does not want.
 */
public class UpdateOperationListenerImpl implements UpdateOperationListener {

	private final WorkflowEventManager workflowEventManager;

	public UpdateOperationListenerImpl(final AbstractSharkService workflowService,
			final WorkflowEventManager workflowEventManager) {
		this.workflowEventManager = workflowEventManager;
		workflowService.setUpdateOperationListener(this);
	}

	@Override
	public void processInstanceStarted(final int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

	@Override
	public void processInstanceAborted(final int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void processInstanceSuspended(final int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void processInstanceResumed(final int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void activityInstanceAborted(final int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

	@Override
	public void activityInstanceAdvanced(final int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void abortedOperation(final int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

}
