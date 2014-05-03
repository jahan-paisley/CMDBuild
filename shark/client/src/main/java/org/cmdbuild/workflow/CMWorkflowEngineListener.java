package org.cmdbuild.workflow;

public interface CMWorkflowEngineListener {

	void syncStarted();

	void syncProcessStarted(CMProcessClass processClass);

	void syncProcessInstanceNotFound(CMProcessInstance processInstance);

	void syncProcessInstanceFound(CMProcessInstance processInstance);

	void syncFinished();

}
