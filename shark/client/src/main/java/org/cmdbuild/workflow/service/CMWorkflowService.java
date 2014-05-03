package org.cmdbuild.workflow.service;

import java.util.Map;

import org.cmdbuild.workflow.CMWorkflowException;

/**
 * Workflow service with low-level operations
 */
public interface CMWorkflowService {

	/**
	 * Retrieves all package versions by package id.
	 * 
	 * @param pkgId
	 * @return all package versions
	 * @throws CMWorkflowException
	 */
	String[] getPackageVersions(String pkgId) throws CMWorkflowException;

	/**
	 * 
	 * @param pkgId
	 *            package id can be null if new
	 * @param pkgDefData
	 * @return uploaded package info
	 * @throws CMWorkflowException
	 */
	WSPackageDefInfo uploadPackage(String pkgId, byte[] pkgDefData) throws CMWorkflowException;

	byte[] downloadPackage(String pkgId, String pkgVer) throws CMWorkflowException;

	/**
	 * Download the last version of every open package
	 * 
	 * @return an array of package versions
	 * @throws CMWorkflowException
	 */
	WSPackageDef[] downloadAllPackages() throws CMWorkflowException;

	/**
	 * Create and start the latest version of a process.
	 * 
	 * @param pkgId
	 *            package id
	 * @param procDefId
	 *            workflow process definition id (as defined in the xpdl)
	 * @return newly created process instance id
	 * @throws CMWorkflowException
	 */
	WSProcessInstInfo startProcess(String pkgId, String procDefId) throws CMWorkflowException;

	/**
	 * Create the latest version of a process, sets the variables, starts it.
	 * 
	 * @param pkgId
	 *            package id
	 * @param procDefId
	 *            workflow process definition id (as defined in the xpdl)
	 * @param variables
	 *            values for variables
	 * @return newly created process instance id
	 * @throws CMWorkflowException
	 */
	WSProcessInstInfo startProcess(String pkgId, String procDefId, Map<String, ?> variables) throws CMWorkflowException;

	/**
	 * List open process instances by process definition id.
	 * 
	 * @param procDefId
	 * @return
	 * @throws CMWorkflowException
	 */
	WSProcessInstInfo[] listOpenProcessInstances(String procDefId) throws CMWorkflowException;

	/**
	 * Retrieve informations about an open process instance.
	 * 
	 * @param procInstId
	 *            process instance id
	 * @return
	 * @throws CMWorkflowException
	 */
	WSProcessInstInfo getProcessInstance(String procInstId) throws CMWorkflowException;

	void setProcessInstanceVariables(String procInstId, Map<String, ?> variables) throws CMWorkflowException;

	/**
	 * Returns the process instance variables.
	 * 
	 * @param procInstId
	 *            process instance id
	 * @return process instance variables
	 * @throws CMWorkflowException
	 */
	Map<String, Object> getProcessInstanceVariables(String procInstId) throws CMWorkflowException;

	/**
	 * Returns a list of open activities for a process instance.
	 * 
	 * @param procInstId
	 * @return list of open activity instances for the process instance
	 * @throws CMWorkflowException
	 */
	WSActivityInstInfo[] findOpenActivitiesForProcessInstance(String procInstId) throws CMWorkflowException;

	/**
	 * Returns a list of open activities for a process definition.
	 * 
	 * @param procDefId
	 * @return list of open activity instances for the process definition
	 * @throws CMWorkflowException
	 */
	WSActivityInstInfo[] findOpenActivitiesForProcess(String procDefId) throws CMWorkflowException;

	/**
	 * Aborts the specified activity, stopping that flow path.
	 * 
	 * @param procInstId
	 *            process instance id
	 * @param actInstId
	 *            activity instance id
	 * @throws CMWorkflowException
	 */
	void abortActivityInstance(String procInstId, String actInstId) throws CMWorkflowException;

	/**
	 * Advances the specified activity, returning when the flow has stopped.
	 * 
	 * @param procInstId
	 *            process instance id
	 * @param actInstId
	 *            activity instance id
	 * @throws CMWorkflowException
	 */
	void advanceActivityInstance(String procInstId, String actInstId) throws CMWorkflowException;

	/**
	 * Aborts the specified process instance
	 * 
	 * @param procInstId
	 *            process instance id
	 * @throws CMWorkflowException
	 */
	void abortProcessInstance(String procInstId) throws CMWorkflowException;

	/**
	 * Suspends the specified process instance
	 * 
	 * @param procInstId
	 *            process instance id
	 * @throws CMWorkflowException
	 */
	void suspendProcessInstance(String procInstId) throws CMWorkflowException;

	/**
	 * Suspends the specified process instance
	 * 
	 * @param procInstId
	 *            process instance id
	 * @throws CMWorkflowException
	 */
	void resumeProcessInstance(String procInstId) throws CMWorkflowException;

	/**
	 * Deletes the specified process instance
	 * 
	 * @param procInstId
	 *            process instance id
	 * @throws CMWorkflowException
	 */
	void deleteProcessInstance(String procInstId) throws CMWorkflowException;

}
