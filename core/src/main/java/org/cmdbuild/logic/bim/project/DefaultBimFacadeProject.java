package org.cmdbuild.logic.bim.project;

import java.io.File;

import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.joda.time.DateTime;

class DefaultBimFacadeProject implements BimFacadeProject {

	private String name;
	private File file;
	private boolean active;
	private String projectId;

	@Override
	public String getProjectId() {
		return projectId;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public DateTime getLastCheckin() {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getFile() {
		return this.file;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setFile(final File file) {
		this.file = file;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	@Override
	public boolean isSynch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastCheckin(final DateTime lastCheckin) {
		throw new UnsupportedOperationException();
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	@Override
	public String getShapeProjectId() {
		throw new UnsupportedOperationException("to do");
	}

	@Override
	public String getExportProjectId() {
		throw new UnsupportedOperationException("to do");
	}

}