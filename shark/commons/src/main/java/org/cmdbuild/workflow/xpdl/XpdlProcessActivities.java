package org.cmdbuild.workflow.xpdl;

import java.util.List;

import org.enhydra.jxpdl.elements.Activities;
import org.enhydra.jxpdl.elements.Activity;

public class XpdlProcessActivities extends XpdlActivities {

	final XpdlProcess xpdlProcess;

	XpdlProcessActivities(final XpdlProcess xpdlProcess) {
		super(xpdlProcess.getDocument());
		this.xpdlProcess = xpdlProcess;
	}

	@Override
	protected Activities activities() {
		return xpdlProcess.inner.getActivities();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<Activity> startingActivities() {
		return (List<Activity>) xpdlProcess.inner.getStartingActivities();
	}

	@Override
	protected XpdlProcess process() {
		return xpdlProcess;
	}

}
