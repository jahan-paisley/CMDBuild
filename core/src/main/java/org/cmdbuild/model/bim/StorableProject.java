package org.cmdbuild.model.bim;

import org.cmdbuild.data.store.Storable;
import org.joda.time.DateTime;

public class StorableProject implements Storable {

	private String projectId, name, description, //
		importMapping, exportMapping, //
		exportProjectId, shapeProjectId;
	private boolean active, synch;
	private DateTime lastCheckin;
	private Long cardId;
	
	
	
	@Override
	public String getIdentifier() {
		return getProjectId();
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public DateTime getLastCheckin() {
		return lastCheckin;
	}

	public void setLastCheckin(final DateTime lastCheckin) {
		this.lastCheckin = lastCheckin;
	}

	public boolean isSynch() {
		return synch;
	}

	public void setSynch(boolean synch) {
		this.synch = synch;
	}

	public String getImportMapping() {
		return importMapping;
	}

	public void setImportMapping(String importMapping) {
		this.importMapping = importMapping;
	}
	
	public void setExportProjectId(final String exportProjectId) {
		this.exportProjectId = exportProjectId;
	}

	public String getExportMapping() {
		return exportMapping;
	}

	public void setExportMapping(String exportMapping) {
		this.exportMapping = exportMapping;
	}

	public Long getCardId() {
		return cardId;
	}

	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}

	public String getExportProjectId() {
		return exportProjectId;
	}

	public String getShapeProjectId() {
		return shapeProjectId;
	}

	public void setShapeProjectId(String shapeProjectId) {
		this.shapeProjectId = shapeProjectId;
	}
}
