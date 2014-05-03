package org.cmdbuild.bim.geometry;

import org.cmdbuild.bim.model.SpaceGeometry;

public interface IfcSpaceGeometryReader {
	
	public SpaceGeometry fetchGeometry(String spaceIdentifier);

}