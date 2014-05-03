package org.cmdbuild.workflow;

import java.util.Map;

/**
 * Like CMDataView but for processes. It joins BPS and data store.
 */
public interface CMWorkflowEngine {

	CMProcessClass findProcessClassById(Long id);

	CMProcessClass findProcessClassByName(String name);

	/**
	 * Returns the active process classes.
	 * 
	 * @return active process classes
	 */
	Iterable<? extends CMProcessClass> findProcessClasses();

	/**
	 * Returns all (active and inactive) process classes.
	 * 
	 * @return all process classes (active and inactive)
	 */
	Iterable<? extends CMProcessClass> findAllProcessClasses();

	/**
	 * Starts a process leaving the activity for the current user and returns
	 * the created process instance.
	 * 
	 * Of course it has no meaning for a BPS, since a process should have no
	 * privileged activity to start. We create the process instance, start it
	 * and terminate every activity that is not the user's own.
	 * 
	 * In the future we might consider adding a map of initial variables to be
	 * set before starting the process, but after it is created.
	 * 
	 * @param type
	 *            process type
	 * @return the only activity instance that is left
	 * @throws CMWorkflowException
	 */
	CMProcessInstance startProcess(CMProcessClass type) throws CMWorkflowException;

	/**
	 * Aborts the process instance.
	 * 
	 * @param processInstance
	 *            process instance
	 * @throws CMWorkflowException
	 */
	void abortProcessInstance(CMProcessInstance processInstance) throws CMWorkflowException;

	/**
	 * Suspends the process instance.
	 * 
	 * @param processInstance
	 *            process instance
	 * @throws CMWorkflowException
	 */
	void suspendProcessInstance(CMProcessInstance processInstance) throws CMWorkflowException;

	/**
	 * Resumes the process instance.
	 * 
	 * @param processInstance
	 *            process instance
	 * @throws CMWorkflowException
	 */
	void resumeProcessInstance(CMProcessInstance processInstance) throws CMWorkflowException;

	/**
	 * Updates the variables of the process instance to which the activity
	 * instance belongs. Executes the default action of each widget defined in
	 * the activity instance. It leaves the activity instance in its previous
	 * state.
	 * 
	 * @param activityInstance
	 *            activity instance object to update
	 * @param vars
	 *            map of values to update
	 * @param widgetSubmission
	 *            map of widget ids and submission objects
	 * @throws CMWorkflowException
	 */
	void updateActivity(CMActivityInstance activityInstance, Map<String, ?> vars, Map<String, Object> widgetSubmission)
			throws CMWorkflowException;

	/**
	 * Advances an activity.
	 * 
	 * @param activityInstance
	 *            activity instance object to advance
	 * @return the updated process instance as it is after the action
	 * @throws CMWorkflowException
	 */
	CMProcessInstance advanceActivity(CMActivityInstance activityInstance) throws CMWorkflowException;

	/**
	 * Synchronizes the local store with the workflow service.
	 * 
	 * @throws CMWorkflowException
	 */
	void sync() throws CMWorkflowException;

	void setEventListener(CMWorkflowEngineListener eventListener);

}
