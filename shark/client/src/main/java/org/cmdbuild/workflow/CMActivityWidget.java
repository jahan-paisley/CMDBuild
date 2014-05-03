package org.cmdbuild.workflow;

import java.util.Map;

public interface CMActivityWidget {

	String getStringId();

	String getLabel();

	boolean isAlwaysenabled();

	/**
	 * Performs some custom action.
	 * 
	 * @param action
	 *            action type
	 * @param params
	 *            action parameters
	 * @param dsVars
	 *            values saved in the data store
	 * @return action output
	 * @throws Exception
	 */
	Object executeAction(String action, Map<String, Object> params, Map<String, Object> dsVars) throws Exception;

	/**
	 * Fills the output variables.
	 * 
	 * Values should be native to CMDBuild, not to the Workflow Engine.
	 * 
	 * @param input
	 *            widget submission object
	 * @param output
	 *            values to be saved into the workflow engine
	 * @throws Exception
	 */
	void save(CMActivityInstance activityInstance, Object input, Map<String, Object> output) throws Exception;

	/**
	 * React to the activity advancement.
	 * 
	 * This can be used by widgets that need to do something only when the
	 * activity advances, like sending emails, etc.
	 * 
	 * @throws Exception
	 */
	void advance(CMActivityInstance activityInstance);

}