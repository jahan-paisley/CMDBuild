package org.cmdbuild.logger;

import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowEngineListener;

public class WorkflowLogger implements CMWorkflowEngineListener {

	@Override
	public void syncStarted() {
		if (Log.WORKFLOW.isInfoEnabled()) {
			Log.WORKFLOW.info("Begin global process sync");
		}
	}

	@Override
	public void syncProcessStarted(final CMProcessClass processClass) {
		if (Log.WORKFLOW.isInfoEnabled()) {
			Log.WORKFLOW.info("Begin process sync: " + processClass.getName());
		}
	}

	@Override
	public void syncProcessInstanceNotFound(final CMProcessInstance processInstance) {
		if (Log.WORKFLOW.isInfoEnabled()) {
			Log.WORKFLOW.info(String.format("Process instance %s not found: removing card %s",
					processInstance.getProcessInstanceId(), processInstance.getCardId()));
		}
	}

	@Override
	public void syncProcessInstanceFound(final CMProcessInstance processInstance) {
		if (Log.WORKFLOW.isDebugEnabled()) {
			Log.WORKFLOW.debug(String.format("Process instance %s found: updating card %s",
					processInstance.getProcessInstanceId(), processInstance.getCardId()));
		}
	}

	@Override
	public void syncFinished() {
		if (Log.WORKFLOW.isInfoEnabled()) {
			Log.WORKFLOW.info("End global process sync");
		}
	}
}
