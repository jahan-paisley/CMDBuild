package org.cmdbuild.workflow.user;

import org.cmdbuild.workflow.CMActivityInstance;

/**
 * Activity instance used by a user
 */
public interface UserActivityInstance extends CMActivityInstance {

	/**
	 * The current user can modify this activity instance.
	 * 
	 * @return if it is modifiable
	 */
	boolean isWritable();

	@Override
	UserProcessInstance getProcessInstance();
}