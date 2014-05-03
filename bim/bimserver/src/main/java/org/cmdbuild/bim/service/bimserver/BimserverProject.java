package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SObjectState;
import org.bimserver.interfaces.objects.SProject;
import org.cmdbuild.bim.service.BimProject;
import org.joda.time.DateTime;

public class BimserverProject implements BimProject {

	private static final String ACTIVE = "ACTIVE";
	private final SProject project;
	private DateTime lastCheckin;

	protected BimserverProject(final SProject project) {
		this.project = project;
	}

	@Override
	public String getIdentifier() {
		final long poid = project.getOid();
		return String.valueOf(poid);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isActive() {
		final SObjectState state = project.getState();
		return state.name().equals(ACTIVE);
	}

	@Override
	public String toString() {
		return project.getOid() + " " + project.getName();
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public DateTime getLastCheckin() {
		return lastCheckin;
	}

	@Override
	public void setLastCheckin(final DateTime lastCheckin) {
		this.lastCheckin = lastCheckin;
	}

}
