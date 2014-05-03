package org.cmdbuild.workflow.xpdl;

import java.util.List;

public interface XpdlActivityHolder {

	/**
	 * Creates and adds a new activity to this element.
	 * 
	 * @param activity id
	 * @return the created activity
	 */
	XpdlActivity createActivity(String activityId);

	/**
	 * Retrieves the activity by that id.
	 * 
	 * @param activity id
	 * @return the activity by that id
	 */
	XpdlActivity getActivity(String activityId);

	/**
	 * Get the starting activities for this element (those that have no
	 * incoming transition).
	 * 
	 * @return list of starting activities
	 */
	List<XpdlActivity> getStartActivities();

	List<XpdlActivity> getManualStartActivitiesRecursive();
}