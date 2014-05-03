package org.cmdbuild.workflow.user;

import java.util.List;

import org.cmdbuild.workflow.CMProcessInstance;

/**
 * Process instance used by a user.
 */
public interface UserProcessInstance extends CMProcessInstance {

	@Override
	List<UserActivityInstance> getActivities();

	@Override
	UserActivityInstance getActivityInstance(String activityInstanceId);

}
