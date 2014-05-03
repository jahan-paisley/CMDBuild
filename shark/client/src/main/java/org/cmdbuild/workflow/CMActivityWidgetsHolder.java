package org.cmdbuild.workflow;

import java.util.List;

public interface CMActivityWidgetsHolder {

	/**
	 * Returns the activity widgets.
	 * 
	 * @return ordered list of widgets for this activity
	 * 
	 * @throws CMWorkflowException
	 */
	List<CMActivityWidget> getWidgets() throws CMWorkflowException;

}
