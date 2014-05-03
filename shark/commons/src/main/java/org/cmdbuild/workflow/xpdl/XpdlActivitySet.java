package org.cmdbuild.workflow.xpdl;

import java.util.List;

import org.enhydra.jxpdl.elements.ActivitySet;

public class XpdlActivitySet implements XpdlActivityHolder {

	final XpdlProcess process;
	final ActivitySet inner;
	private final XpdlActivities activities;

	XpdlActivitySet(final XpdlProcess process, final ActivitySet activitySet) {
		this.process = process;
		this.inner = activitySet;
		this.activities = new XpdlActivitySetActivities(this);
	}

	@Override
	public XpdlActivity createActivity(String activityId) {
		return activities.createActivity(activityId);
	}

	@Override
	public XpdlActivity getActivity(final String activityId) {
		return new XpdlActivity(process, inner.getActivity(activityId));
	}

	@Override
	public List<XpdlActivity> getStartActivities() {
		return activities.getStartActivities();
	}

	@Override
	public List<XpdlActivity> getManualStartActivitiesRecursive() {
		return activities.getManualStartActivitiesRecursive();
	}

}