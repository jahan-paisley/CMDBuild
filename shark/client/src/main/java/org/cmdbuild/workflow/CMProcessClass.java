package org.cmdbuild.workflow;

import javax.activation.DataSource;

import org.cmdbuild.dao.entrytype.CMClass;

/**
 * Class object extended for workflow handling
 */
public interface CMProcessClass extends CMClass {

	/**
	 * Creates a definition template for this process.
	 * 
	 * @return a template process definition versions
	 * @throws CMWorkflowException
	 */
	DataSource getDefinitionTemplate() throws CMWorkflowException;

	/**
	 * Returns the available process definition versions .
	 * 
	 * @return process definition versions
	 * @throws CMWorkflowException
	 */
	String[] getDefinitionVersions() throws CMWorkflowException;

	/**
	 * Returns one version of the definition file for this process.
	 * 
	 * @return process definition
	 * @throws CMWorkflowException
	 */
	DataSource getDefinition(String version) throws CMWorkflowException;

	/**
	 * Associates a package definition to this process
	 * 
	 * @param pkgDefData
	 * @throws CMWorkflowException
	 */
	void updateDefinition(DataSource pkgDefData) throws CMWorkflowException;

	/**
	 * Being stoppable by a user is a property of the process class. For some
	 * reason a few customers don't want this to be defined in the process
	 * workflow but they like this "hack" instead.
	 * 
	 * @return if process can be stopped by every user that can modify it
	 */
	boolean isUserStoppable();

	/**
	 * Returns the start activity for the curent user. It considers the latest
	 * package version that is bound to this process.
	 * 
	 * Normally there is no suck thing as "start activity", so this is an
	 * amazingly awful thing we do.
	 * 
	 * @return start activity for the current user
	 * @throws CMWorkflowException
	 *             if no activity is found
	 */
	CMActivity getStartActivity() throws CMWorkflowException;

	/**
	 * Returns the package id for this process class.
	 * 
	 * @return package id for this process class
	 */
	String getPackageId() throws CMWorkflowException;

	/**
	 * Returns the process definition id for this process class.
	 * 
	 * @return process definition id for this process class
	 */
	String getProcessDefinitionId() throws CMWorkflowException;

	/**
	 * Returns if the process is active and a process definition was loaded for
	 * it.
	 * 
	 * @return if the process can be started or advanced
	 */
	boolean isUsable();
}
