package org.cmdbuild.logic.workflow;

import java.io.IOException;
import java.util.Map;

import javax.activation.DataSource;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;

/**
 * Business Logic Layer for Workflow Operations.
 */
public interface WorkflowLogic extends Logic {

	/**
	 * Ugliness to be used in old code
	 */
	boolean isProcessUsable(String className);

	boolean isWorkflowEnabled();

	PagedElements<UserProcessInstance> query(String className, QueryOptions queryOptions);

	Iterable<UserProcessClass> findAllProcessClasses();

	Iterable<? extends UserProcessClass> findActiveProcessClasses();

	UserProcessClass findProcessClass(String className);

	/*
	 * Management
	 */

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class name
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	CMActivity getStartActivity(String processClassName) throws CMWorkflowException;

	CMActivity getStartActivityOrDie( //
			String processClassName //
	) throws CMWorkflowException, CMDBWorkflowException;

	CMActivity getStartActivityOrDie( //
			Long processClassId //
	) throws CMWorkflowException, CMDBWorkflowException;

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class id
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	CMActivity getStartActivity(Long processClassId) throws CMWorkflowException;

	UserProcessInstance getProcessInstance(String processClassName, Long cardId);

	UserProcessInstance getProcessInstance(Long processClassId, Long cardId);

	UserActivityInstance getActivityInstance(String processClassName, Long processCardId, String activityInstanceId);

	UserActivityInstance getActivityInstance(Long processClassId, Long processCardId, String activityInstanceId);

	/**
	 * Retrieve the processInstance and check if the given date is the same of
	 * the process begin date in this case, we assume that the process is
	 * updated
	 * 
	 * @param processClassName
	 * @param processInstanceId
	 * @param givenBeginDate
	 * @return
	 */
	boolean isProcessUpdated( //
			String processClassName, //
			Long processInstanceId, //
			DateTime givenBeginDate //
	);

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassName
	 *            process class name
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	UserProcessInstance startProcess(String processClassName, Map<String, ?> vars,
			Map<String, Object> widgetSubmission, boolean advance) throws CMWorkflowException;

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassId
	 *            process class id
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	UserProcessInstance startProcess(Long processClassId, Map<String, ?> vars, Map<String, Object> widgetSubmission,
			boolean advance) throws CMWorkflowException;

	UserProcessInstance updateProcess(String processClassName, Long processCardId, String activityInstanceId,
			Map<String, ?> vars, Map<String, Object> widgetSubmission, boolean advance) throws CMWorkflowException;

	UserProcessInstance updateProcess(Long processClassId, Long processCardId, String activityInstanceId,
			Map<String, ?> vars, Map<String, Object> widgetSubmission, boolean advance) throws CMWorkflowException;

	void suspendProcess(String processClassName, Long processCardId) throws CMWorkflowException;

	void resumeProcess(String processClassName, Long processCardId) throws CMWorkflowException;

	/*
	 * Administration
	 */

	void sync() throws CMWorkflowException;

	DataSource getProcessDefinitionTemplate(Long processClassId) throws CMWorkflowException;

	String[] getProcessDefinitionVersions(Long processClassId) throws CMWorkflowException;

	DataSource getProcessDefinition(Long processClassId, String version) throws CMWorkflowException;

	void updateProcessDefinition(Long processClassId, DataSource xpdlFile) throws CMWorkflowException;

	void removeSketch(Long processClassId);

	void addSketch(Long processClassId, DataSource ds) throws IOException;

	void abortProcess(Long processClassId, long processCardId) throws CMWorkflowException;

}