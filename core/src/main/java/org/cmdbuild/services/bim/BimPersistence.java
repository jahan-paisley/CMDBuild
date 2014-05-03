package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.StorableLayer;
import org.joda.time.DateTime;

public interface BimPersistence {

	public interface PersistenceProject {

		String getProjectId();

		String getName();

		String getDescription();

		Long getCmId();

		boolean isActive();

		boolean isSynch();

		String getImportMapping();

		String getExportMapping();

		String getExportProjectId();

		DateTime getLastCheckin();

		Iterable<String> getCardBinding();

		void setSynch(boolean synch);

		void setProjectId(String projectId);

		void setLastCheckin(DateTime lastCheckin);

		void setName(String name);

		void setDescription(String description);

		void setCardBinding(Iterable<String> cardBinding);

		void setActive(boolean active);

		String getShapeProjectId();

		void setExportProjectId(String projectId);

	}

	void saveProject(PersistenceProject project);

	Iterable<PersistenceProject> readAll();

	PersistenceProject read(String projectId);

	void disableProject(PersistenceProject project);

	void enableProject(PersistenceProject project);

	Iterable<StorableLayer> listLayers();

	void saveActiveFlag(String className, String value);

	void saveRootFlag(String className, boolean value);

	void saveExportFlag(String className, String value);

	void saveContainerFlag(String className, String value);

	void saveRootReferenceName(String className, String value);

	StorableLayer findRoot();

	StorableLayer findContainer();

	String getProjectIdFromCardId(Long cardId);

	Long getCardIdFromProjectId(String projectId);

	boolean isActiveLayer(String classname);

	String getContainerClassName();

	StorableLayer readLayer(String className);

}
