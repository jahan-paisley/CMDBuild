package org.cmdbuild.workflow;

import java.util.List;

public interface CMActivityInstance extends CMActivityWidgetsHolder {

	CMProcessInstance getProcessInstance();

	String getId();

	CMActivity getDefinition() throws CMWorkflowException;

	String getPerformerName();

	/**
	 * Returns the activity widgets for this process instance, with expansion of
	 * "server" variables.
	 * 
	 * @return ordered list of widgets for this activity instance
	 * @throws CMWorkflowException
	 */
	@Override
	List<CMActivityWidget> getWidgets() throws CMWorkflowException;
}
