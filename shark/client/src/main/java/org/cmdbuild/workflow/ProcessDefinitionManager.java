package org.cmdbuild.workflow;

import javax.activation.DataSource;

/**
 * Handles all the operations on workflow definition files, including template
 * generation, download and upload from/to the workflow engine.
 */
public interface ProcessDefinitionManager {

	/**
	 * Returns a new process definition document for the process
	 * 
	 * @param process
	 * @return template document
	 * @throws CMWorkflowException
	 */
	DataSource getTemplate(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns the process definition versions available in the repository. They
	 * can be in use or not.
	 * 
	 * @param process
	 * @return list of process definition versions
	 */
	String[] getVersions(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns one version of the process definition document for the process.
	 * 
	 * @param process
	 * @param version
	 * @return document
	 * @throws CMWorkflowException
	 */
	DataSource getDefinition(CMProcessClass process, String version) throws CMWorkflowException;

	/**
	 * Associates a package definition to a process
	 * 
	 * @param process
	 * @param pkgDefData
	 * @throws CMWorkflowException
	 */
	void updateDefinition(CMProcessClass process, DataSource pkgDefData) throws CMWorkflowException;

	/**
	 * Gets the first process activity for a group. It is totally a CMDBuild
	 * customization, since the workflow engine starts every starting activity.
	 * 
	 * @param process
	 * @param name
	 *            of the group that is going to start the process
	 * @return activity definition
	 * @throws CMWorkflowException
	 */
	CMActivity getManualStartActivity(CMProcessClass process, String groupName) throws CMWorkflowException;

	/**
	 * Gets the activity definition by process instance and identifier.
	 * 
	 * @param processInstance
	 *            process instance
	 * @param activityDefinitionId
	 *            activity definition id
	 * @return activity definition
	 * @throws CMWorkflowException
	 */
	CMActivity getActivity(CMProcessInstance processInstance, String activityDefinitionId) throws CMWorkflowException;

	/**
	 * Returns the package id for the process.
	 * 
	 * @param process
	 * @return package id
	 */
	String getPackageId(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns the process definition id for the process.
	 * 
	 * @param process
	 * @return process definition id
	 */
	String getProcessDefinitionId(CMProcessClass process) throws CMWorkflowException;

	/**
	 * Returns the process class name for that process definition id.
	 * 
	 * The signature should be changed to return a CMProcessClass.
	 * 
	 * @param process
	 *            definition id
	 * @return process class name
	 */
	String getProcessClassName(String processDefinitionId) throws CMWorkflowException;

}
