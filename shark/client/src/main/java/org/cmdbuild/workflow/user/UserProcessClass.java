package org.cmdbuild.workflow.user;

import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;

/**
 * Process class used by a user
 */
public interface UserProcessClass extends CMProcessClass {

	/**
	 * @return if the process is stoppable by the current user
	 */
	boolean isStoppable();

	/**
	 * @return if the process is startable by the current user
	 */
	boolean isStartable() throws CMWorkflowException;

}
