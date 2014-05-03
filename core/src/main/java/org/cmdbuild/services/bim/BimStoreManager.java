package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.model.bim.StorableProject;

public interface BimStoreManager {
	
	Iterable<StorableProject> readAll();
	
	StorableProject read(final String identifier);

	void write(final StorableProject project);
	
	void disableProject(final String identifier);
	
	void enableProject(final String identifier);

	Iterable<StorableLayer> readAllLayers();
	
	void saveActiveStatus(String className, String value);

	void saveRoot(String className, boolean value);
	
	void saveExportStatus(String className, String value);
	
	void saveContainerStatus(String className, String value);
	
	void saveRootReference(String className, String value);

	boolean isActive(String className);
	
	String getContainerClassName();

	StorableLayer findRoot();

	StorableLayer findContainer();

	StorableLayer readLayer(String className);

}
