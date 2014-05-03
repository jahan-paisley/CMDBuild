package org.cmdbuild.logic.bim.project;

import java.io.File;

import org.cmdbuild.logic.bim.project.ProjectLogic.Project;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.joda.time.DateTime;

class ProjectWrapper implements Project {

	private final PersistenceProject delegate;

	public ProjectWrapper(PersistenceProject delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getProjectId() {
		return delegate.getProjectId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	@Override
	public boolean isSynch() {
		return delegate.isSynch();
	}

	@Override
	public String getImportMapping() {
		return delegate.getImportMapping();
	}

	@Override
	public String getExportMapping() {
		return delegate.getExportMapping();
	}

	@Override
	public DateTime getLastCheckin() {
		return delegate.getLastCheckin();
	}

	@Override
	public Iterable<String> getCardBinding() {
		return delegate.getCardBinding();
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException();
	}

}
