package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

/**
 * Immutable process instance.
 */
public interface CMProcessInstance extends CMCard {

	/**
	 * {@link CMProcessInstance} mutator object. FIXME Unlucky name :(
	 */
	interface CMProcessInstanceDefinition extends CMCardDefinition {

		@Override
		CMProcessInstanceDefinition set(String key, Object value);

		CMProcessInstanceDefinition setActivities(WSActivityInstInfo[] activityInfos) throws CMWorkflowException;

		CMProcessInstanceDefinition addActivity(WSActivityInstInfo activityInfo) throws CMWorkflowException;

		CMProcessInstanceDefinition removeActivity(String activityInstanceId) throws CMWorkflowException;

		CMProcessInstanceDefinition setState(WSProcessInstanceState state);

		/**
		 * Updates the Used by service synchronization.
		 * 
		 * @param process
		 *            definition information
		 * @return the {@link CMProcessInstanceDefinition} itself for chaining
		 */
		CMProcessInstanceDefinition setUniqueProcessDefinition(WSProcessDefInfo info);

		/**
		 * Save the process instance if something has changed
		 */
		@Override
		CMProcessInstance save();

	}

	/**
	 * It should return {@link CMCard.getId()}. It is used to disambiguate
	 * between the card and process instance ids.
	 * 
	 * @return identifier of the data store card
	 */
	Long getCardId();

	/**
	 * We cannot override {@link CMCard.getId()} because it would break the
	 * method semantics.
	 * 
	 * @return identifier of the process instance
	 */
	String getProcessInstanceId();

	@Override
	CMProcessClass getType();

	/**
	 * Get current process instance state.
	 * 
	 * @return the current process state
	 */
	WSProcessInstanceState getState();

	/**
	 * Get all the process instance activities.
	 * 
	 * @return the process instance activities
	 */
	List<? extends CMActivityInstance> getActivities();

	/**
	 * Get an activity by its id.
	 * 
	 * @param activityInstanceId
	 * @return activity instance
	 */
	CMActivityInstance getActivityInstance(String activityInstanceId);

	/**
	 * Returns an object with the ids to uniquely identify a process definition.
	 * 
	 * @return unique process definition informations
	 */
	WSProcessDefInfo getUniqueProcessDefinition();

}
