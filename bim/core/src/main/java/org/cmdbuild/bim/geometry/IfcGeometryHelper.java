package org.cmdbuild.bim.geometry;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;

public interface IfcGeometryHelper {

	Vector3d getCoordinatesOfIfcCartesianPoint(Entity ifcCartesianPoint);

	Position3d getPositionFromIfcPlacement(Entity ifcPlacement);

	Position3d getAbsoluteObjectPlacement(Entity ifcProduct);

	Vector3d computeCentroidFromPolyline(List<Position3d> polylineVertices);

	Double computeWidthFromPolyline(List<Position3d> polylineVertices);

	Double computeHeightFromPolyline(List<Position3d> polylineVertices);

	SpaceGeometry fetchGeometry(String spaceIdentifier);

}
