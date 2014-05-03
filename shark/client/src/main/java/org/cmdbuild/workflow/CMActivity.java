package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

/**
 * Definition of a process activity.
 */
public interface CMActivity extends CMActivityWidgetsHolder {

	String getId();

	String getDescription();

	String getInstructions();

	/**
	 * Returns the performers defined for this activity.
	 * 
	 * @return list of defined performers
	 */
	List<ActivityPerformer> getPerformers();

	/**
	 * Returns the first non-admin performer defined for this activity.
	 * 
	 * @return role or expression performer
	 */
	ActivityPerformer getFirstNonAdminPerformer();

	/**
	 * Returns an ordered list of variables to be displayed on the form.
	 * 
	 * @return
	 */
	List<CMActivityVariableToProcess> getVariables();

	/**
	 * Returns the activity widgets without the expansion of "server" variables,
	 * since there is no process where to get them from.
	 * 
	 * @return ordered list of widgets for this activity
	 * 
	 * @throws CMWorkflowException
	 */
	@Override
	List<CMActivityWidget> getWidgets() throws CMWorkflowException;

	/**
	 * Returns the activity widgets with the expansion of "server" variables.
	 * 
	 * @param processInstanceVariables
	 * @return ordered list of widgets for this activity
	 */
	List<CMActivityWidget> getWidgets(CMValueSet processInstanceVariables);
}
