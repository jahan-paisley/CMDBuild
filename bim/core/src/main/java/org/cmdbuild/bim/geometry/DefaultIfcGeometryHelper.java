package org.cmdbuild.bim.geometry;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.cmdbuild.bim.model.implementation.IfcPosition3d;
import org.cmdbuild.bim.model.implementation.DefaultSpaceGeometry;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;

import static org.cmdbuild.bim.utils.BimConstants.*;

import com.google.common.collect.Lists;

public class DefaultIfcGeometryHelper implements IfcGeometryHelper {

	private final String revisionId;
	private final BimService service;

	public DefaultIfcGeometryHelper(BimService service, String revisionId) {
		this.service = service;
		this.revisionId = revisionId;
	}

	@Override
	public Position3d getPositionFromIfcPlacement(Entity entity) {
		Position3d position = Position3d.DEFAULT_POSITION;
		Vector3d origin = getOriginOfIfcPlacement(entity);
		position = new IfcPosition3d(origin);

		if (!entity.getTypeName().equals(IFC_AXIS2_PLACEMENT2D) && !entity.getTypeName().equals(IFC_AXIS2_PLACEMENT3D)) {
			Exception e = new Exception();
			throw new BimError("Method " + e.getStackTrace()[0] + " not allowed for " + entity.getTypeName(), e);
		}

		Vector3d e1 = new Vector3d(1, 0, 0);
		Vector3d e3 = new Vector3d(0, 0, 1);
		Attribute e1Ref = entity.getAttributeByName(IFC_REF_DIRECTION);
		Attribute e3Ref = entity.getAttributeByName(IFC_AXIS);
		if (e1Ref.isValid()) {
			Entity e1Entity = service.getReferencedEntity((ReferenceAttribute) e1Ref, revisionId);
			e1 = getVectorFromIfcDirection(e1Entity);
			if (e3Ref.isValid()) {
				Entity e3Entity = service.getReferencedEntity((ReferenceAttribute) e3Ref, revisionId);
				e3 = getVectorFromIfcDirection(e3Entity);
			}
			position = new IfcPosition3d(origin, e1, e3);
		}
		return position;
	}

	@Override
	public Vector3d getCoordinatesOfIfcCartesianPoint(Entity ifcCartesianPoint) {
		if (!ifcCartesianPoint.getTypeName().equals(IFC_CARTESIAN_POINT)) {
			Exception e = new Exception();
			throw new BimError(
					"Method " + e.getStackTrace()[0] + " not allowed for " + ifcCartesianPoint.getTypeName(), e);
		}
		Vector3d pointCoordinates = new Vector3d(0, 0, 0);
		Attribute coordinatesAttribute = ifcCartesianPoint.getAttributeByName(IFC_COORDINATES);
		if (coordinatesAttribute.isValid()) {
			ListAttribute coordinatesList = (ListAttribute) coordinatesAttribute;
			List<Attribute> a = coordinatesList.getValues();
			boolean is2D = a.size() == 2;
			SimpleAttribute x = (SimpleAttribute) a.get(0);
			SimpleAttribute y = (SimpleAttribute) a.get(1);
			pointCoordinates.x = Double.parseDouble(x.getValue());
			pointCoordinates.y = Double.parseDouble(y.getValue());
			if (!is2D) {
				SimpleAttribute z = (SimpleAttribute) a.get(2);
				pointCoordinates.z = Double.parseDouble(z.getValue());
			}
		}
		return pointCoordinates;
	}

	@Override
	public Position3d getAbsoluteObjectPlacement(Entity entity) {
		Position3d absolutePlacement = Position3d.DEFAULT_POSITION;
		List<Position3d> placements = Lists.newArrayList();
		Attribute objectPlacementRef = entity.getAttributeByName(IFC_OBJECT_PLACEMENT);
		if (objectPlacementRef.isValid()) {
			Entity localPlacement = service.getReferencedEntity((ReferenceAttribute) objectPlacementRef, revisionId);
			if (localPlacement.isValid() && localPlacement.getTypeName().equals("IfcLocalPlacement")) {
				findAllNestedPlacements(localPlacement, placements, revisionId);
			}
			if (placements.size() > 0) {
				convertPositions(placements);
				absolutePlacement = placements.get(0);
			}
		}
		return absolutePlacement;
	}

	private Vector3d getOriginOfIfcPlacement(Entity ifcplacement) {
		Vector3d origin = new Vector3d(0, 0, 0);
		if (!ifcplacement.getTypeName().equals(IFC_AXIS2_PLACEMENT3D)
				&& !ifcplacement.getTypeName().equals(IFC_AXIS2_PLACEMENT2D)) {
			Exception e = new Exception();
			throw new BimError("Method " + e.getStackTrace()[0] + " not allowed for " + ifcplacement.getTypeName(), e);
		}
		Attribute locationRef = ifcplacement.getAttributeByName("Location");
		if (!locationRef.isValid()) {
			return origin;
		}
		Entity locationEntity = service.getReferencedEntity((ReferenceAttribute) locationRef, revisionId);
		if (!locationEntity.isValid()) {
			return origin;
		}
		origin = getCoordinatesOfIfcCartesianPoint(locationEntity);
		return origin;
	}

	private void convertPositions(List<Position3d> placements) {
		for (int i = placements.size() - 1; i > 0; i--) {
			Position3d system = placements.get(i);
			system.convertToAbsolutePosition(placements.get(i - 1).getOrigin());
		}
	}

	private void findAllNestedPlacements(Entity localPlacement, List<Position3d> placements, String revisionId) {

		Attribute relativePlacementRef = localPlacement.getAttributeByName(IFC_RELATIVE_PLACEMENT);
		if (relativePlacementRef.isValid()) {
			Entity relativePlacement = service.getReferencedEntity((ReferenceAttribute) relativePlacementRef,
					revisionId);
			if (relativePlacement.isValid()) {
				Position3d position = getPositionFromIfcPlacement(relativePlacement);
				placements.add(position);
			}
		}
		Attribute placementRelativeToRef = localPlacement.getAttributeByName(IFC_PLACEMENT_REL_TO);
		if (placementRelativeToRef.isValid()) {
			Entity placementRelativeTo = service.getReferencedEntity((ReferenceAttribute) placementRelativeToRef,
					revisionId);
			if (placementRelativeTo.isValid()) {
				findAllNestedPlacements(placementRelativeTo, placements, revisionId);
			}
		}
	}

	private Vector3d getVectorFromIfcDirection(Entity entity) {
		if (!entity.getTypeName().equals(IFC_DIRECTION)) {
			Exception e = new Exception();
			throw new BimError("Method " + e.getStackTrace()[0] + " not allowed for " + entity.getTypeName(), e);
		}

		Vector3d direction = new Vector3d();
		if (entity.getTypeName().equals(IFC_DIRECTION)) {
			Attribute directionRatiosAttribute = entity.getAttributeByName(IFC_DIRECTION_RATIOS);
			ListAttribute directionRatiosList = (ListAttribute) directionRatiosAttribute;
			List<Attribute> directionRatios = directionRatiosList.getValues();
			boolean is2D = directionRatios.size() == 2;
			direction = new Vector3d(Double.parseDouble(directionRatios.get(0).getValue()),
					Double.parseDouble(directionRatios.get(1).getValue()), 0);
			if (!is2D) {
				direction.z = Double.parseDouble(directionRatios.get(2).getValue());
			}
		}
		return direction;
	}

	@Override
	public Vector3d computeCentroidFromPolyline(List<Position3d> polylineVertices) {
		Vector3d centroid = new Vector3d(0, 0, 0);
		double xmin = 0;
		double xmax = 0;
		double ymin = 0;
		double ymax = 0;
		for (Position3d position : polylineVertices) {
			Vector3d point = position.getOrigin();
			if (point.x < xmin) {
				xmin = point.x;
			}
			if (point.x > xmax) {
				xmax = point.x;
			}
			if (point.y < ymin) {
				ymin = point.y;
			}
			if (point.y > ymax) {
				ymax = point.y;
			}
		}
		centroid.x = (xmin + xmax) / 2;
		centroid.y = (ymin + ymax) / 2;
		return centroid;
	}

	private void setBoundingBox(List<Position3d> polylineVertices, SpaceGeometry geometry) {
		double xmin = polylineVertices.get(0).getOrigin().x;
		double xmax = polylineVertices.get(0).getOrigin().x;
		double ymin = polylineVertices.get(0).getOrigin().y;
		double ymax = polylineVertices.get(0).getOrigin().y;
		for (Position3d position : polylineVertices) {
			Vector3d point = position.getOrigin();
			if (point.x < xmin) {
				xmin = point.x;
			}
			if (point.x > xmax) {
				xmax = point.x;
			}
			if (point.y < ymin) {
				ymin = point.y;
			}
			if (point.y > ymax) {
				ymax = point.y;
			}
		}
		geometry.setXDim(xmax - xmin);
		geometry.setYDim(ymax - ymin);
	}

	@Override
	public Double computeWidthFromPolyline(List<Position3d> polylineVertices) {
		SpaceGeometry geometry = new DefaultSpaceGeometry();
		setBoundingBox(polylineVertices, geometry);
		return geometry.getXDim();
	}

	@Override
	public Double computeHeightFromPolyline(List<Position3d> polylineVertices) {
		SpaceGeometry geometry = new DefaultSpaceGeometry();
		setBoundingBox(polylineVertices, geometry);
		return geometry.getYDim();
	}

	@Override
	public SpaceGeometry fetchGeometry(String spaceIdentifier) {
		DefaultIfcSpaceGeometryReader geometryReader = new DefaultIfcSpaceGeometryReader(service, revisionId);
		return geometryReader.fetchGeometry(spaceIdentifier);
	}

}
