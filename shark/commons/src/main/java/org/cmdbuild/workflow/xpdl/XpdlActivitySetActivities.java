package org.cmdbuild.workflow.xpdl;

import java.util.List;

import org.enhydra.jxpdl.elements.Activities;
import org.enhydra.jxpdl.elements.Activity;

public class XpdlActivitySetActivities extends XpdlActivities {

	final XpdlActivitySet xpdlActivitySet;

	XpdlActivitySetActivities(final XpdlActivitySet xpdlActivitySet) {
		super(xpdlActivitySet.process.getDocument());
		this.xpdlActivitySet = xpdlActivitySet;
	}

	@Override
	protected Activities activities() {
		return xpdlActivitySet.inner.getActivities();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<Activity> startingActivities() {
		return (List<Activity>) xpdlActivitySet.inner.getStartingActivities();
	}

	@Override
	protected XpdlProcess process() {
		return xpdlActivitySet.process;
	}

}
