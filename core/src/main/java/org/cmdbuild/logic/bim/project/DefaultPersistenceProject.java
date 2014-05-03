package org.cmdbuild.logic.bim.project;

import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.joda.time.DateTime;

public class DefaultPersistenceProject implements PersistenceProject {

	private Long cmId;
	private String name, description, importMapping, exportMapping, projectId;
	private boolean sync, active;
	private DateTime lastCheckin;
	private Iterable<String> cardBinding;
	private String exportProjectId;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getImportMapping() {
		return importMapping;
	}

	@Override
	public String getExportMapping() {
		return exportMapping;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isSynch() {
		return sync;
	}

	@Override
	public DateTime getLastCheckin() {
		return lastCheckin;
	}

	@Override
	public Iterable<String> getCardBinding() {
		return this.cardBinding;
	}

	@Override
	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	@Override
	public void setLastCheckin(final DateTime lastCheckin) {
		this.lastCheckin = lastCheckin;
	}

	@Override
	public void setSynch(final boolean sync) {
		this.sync = sync;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

	@Override
	public Long getCmId() {
		return cmId;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public void setCardBinding(final Iterable<String> cardBinding) {
		this.cardBinding = cardBinding;
	}

	@Override
	public void setActive(final boolean active) {
		this.active = active;
	}

	@Override
	public String getExportProjectId() {
		return exportProjectId;
	}

	@Override
	public String getShapeProjectId() {
		throw new UnsupportedOperationException("to do");
	}

	@Override
	public void setExportProjectId(final String projectId) {
		this.exportProjectId = projectId;
	}
}