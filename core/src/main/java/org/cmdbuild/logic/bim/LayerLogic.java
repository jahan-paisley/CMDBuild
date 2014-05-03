package org.cmdbuild.logic.bim;

import org.cmdbuild.logic.Logic;

public interface LayerLogic extends Logic {

	public static interface Layer {

		String getClassName();

		boolean isRoot();

		boolean isContainer();

		String getRootReference();
		
		boolean isExport();
		
		boolean isActive();

		String getDescription();
	}

	Iterable<Layer> readLayers();

	Iterable<Layer> getActiveLayers();

	void updateBimLayer(String className, String attributeName, String value);

	Layer getRootLayer();

	boolean isActive(String classname);

}
