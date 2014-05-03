package org.cmdbuild.workflow;

public class NullWorkflowEngineListener implements CMWorkflowEngineListener {

	@Override
	public void syncStarted() {
	}

	@Override
	public void syncProcessStarted(final CMProcessClass processClass) {
	}

	@Override
	public void syncProcessInstanceNotFound(final CMProcessInstance processInstance) {
	}

	@Override
	public void syncProcessInstanceFound(final CMProcessInstance processInstance) {
	}

	@Override
	public void syncFinished() {
	}

}
