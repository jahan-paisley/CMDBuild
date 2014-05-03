package org.cmdbuild.bim.geometry;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.cmdbuild.bim.model.implementation.DefaultSpaceGeometry;
import org.cmdbuild.bim.model.implementation.IfcPosition3d;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class DefaultIfcSpaceGeometryReader implements IfcSpaceGeometryReader {

	private static final Logger logger = LoggerSupport.geom_logger;

	private final String revisionId;
	private final BimService service;
	private final IfcGeometryHelper geomHelper;
	private String key;

	public DefaultIfcSpaceGeometryReader(BimService service, String revisionId) {
		this.service = service;
		this.revisionId = revisionId;
		geomHelper = new DefaultIfcGeometryHelper(service, revisionId);
	}

	public SpaceGeometry fetchGeometry(String spaceIdentifier) {
		SpaceGeometry geometry = new DefaultSpaceGeometry();
		key = spaceIdentifier;
		Entity space = service.getEntityByGuid(revisionId, key, null);
		System.out.println("");
		System.out.println("");
		System.out.println("Space: " + key + " Name: " + space.getAttributeByName("Name").getValue());
		if (!space.isValid()) {
			throw new BimError("No space found with given identifier");
		}
		IfcGeometryHelper geometryHelper = new DefaultIfcGeometryHelper(service, revisionId);

		// 1. Compute absolute coordinates of the space
		Position3d spacePosition = geometryHelper.getAbsoluteObjectPlacement(space);
		System.out.println(spacePosition.toString());

		// 2. Read representation
		Attribute representationAttribute = space.getAttributeByName("Representation");
		if (representationAttribute.isValid()) {
			Entity shape = service.getReferencedEntity((ReferenceAttribute) representationAttribute, revisionId);
			if (!shape.isValid()) {
				throw new BimError("No shape found for given space");
			}
			Attribute representationList = shape.getAttributeByName("Representations");
			if (!representationList.isValid()) {
				throw new BimError("No valid representation found in shape");
			}
			int indexOfBB = getIndexOfBB(representationList);
			boolean isThereABB = indexOfBB != -1;
			System.out.println("Is there a Bounding Box? " + isThereABB);
			if (isThereABB) {
				System.out.println("Index of BoundingBox : " + indexOfBB);

				Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(indexOfBB),
						revisionId);

				geometry = getGeometryFromBoundingBox(representation);
				System.out.println("Relative coordinates of centroid: " + geometry.getCentroid());

				Position3d absolutePlacement = geomHelper.getAbsoluteObjectPlacement(space);
				System.out.println("Absolute placement of space: " + absolutePlacement);

				convertCoordinates(geometry.getCentroid(), absolutePlacement);
				System.out.println("Absolute coordinates of centroid: " + geometry.getCentroid());
			} else {
				Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(0), revisionId);
				Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
				if (typeAttribute.isValid()) {
					SimpleAttribute type = (SimpleAttribute) typeAttribute;
					if (type.getValue().equals("SweptSolid")) {
						geometry = getGeometryFromSweptSolid(representation);
					}else {
						System.out.println("Import of " + type + " geometries not managed yet");
						return null;
					}
				}
				System.out.println("Base profile vertices: " + geometry.getVertexList());
				System.out.println("Absolute placement of space: " + spacePosition);

				// 5. Convert base profile vertices to the global reference
				// system
				convertCoordinatesAsVectors(geometry.getVertexList(), spacePosition);
				System.out.println("Absolute coordinates of base profile vertices: " + geometry.getVertexList());
			}
		}
		return geometry;
	}

	private SpaceGeometry getGeometryFromBoundingBox(Entity representation) {
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		Entity boundingBox = service.getReferencedEntity(item, revisionId);
		if (!boundingBox.getTypeName().equals("IfcBoundingBox")) {
			throw new BimError(
					"This is not an IfcBoundingBox. This is an unexpected problem, I do not know what to do.");
		}
		Attribute xDim = boundingBox.getAttributeByName("XDim");
		Attribute yDim = boundingBox.getAttributeByName("YDim");
		Attribute zDim = boundingBox.getAttributeByName("ZDim");
		Attribute cornerAttribute = boundingBox.getAttributeByName("Corner");
		if (!xDim.isValid() || !yDim.isValid() || !zDim.isValid() || !cornerAttribute.isValid()) {
			throw new BimError("Some attribute of the Bounding Box is not filled. I do not know what to do.");
		}
		Double dx = Double.parseDouble(xDim.getValue());
		Double dy = Double.parseDouble(yDim.getValue());
		Double dz = Double.parseDouble(zDim.getValue());

		Entity cornerPoint = service.getReferencedEntity((ReferenceAttribute) cornerAttribute, revisionId);
		Vector3d corner = geomHelper.getCoordinatesOfIfcCartesianPoint(cornerPoint);

		double[] centroidAsArray = { corner.x + dx / 2, corner.y + dy / 2, corner.z + dz / 2 };

		Vector3d centroid = new Vector3d(centroidAsArray);
		return new DefaultSpaceGeometry(centroid, dx, dy, dz);
	}

	private SpaceGeometry getGeometryFromSweptSolid(Entity representation) {
		System.out.println("Get geometry from Swept Solid...");
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		Entity sweptSolid = service.getReferencedEntity(item, revisionId);
		if (!sweptSolid.getTypeName().equals("IfcExtrudedAreaSolid")) {
			throw new BimError("This is not an IfcExtrudedAreaSolid. I do not know what to do.");
		}
		Attribute positionAttribute = sweptSolid.getAttributeByName("Position");
		if (!positionAttribute.isValid()) {
			throw new BimError("Position attribute not found");
		}
		Entity position = service.getReferencedEntity((ReferenceAttribute) positionAttribute, revisionId);

		// **** 1. SWEPT SOLID POSITION - It is an IfcPosition3D of the form
		// [O,[M]]
		Position3d sweptSolidPosition = geomHelper.getPositionFromIfcPlacement(position);
		System.out.println("IfcExtrudedAreaSolid.Position " + sweptSolidPosition);

		// **** 2. DEPTH - It is a number
		Attribute depth = sweptSolid.getAttributeByName("Depth");
		Double dz = new Double(0);
		if (depth.isValid()) {
			dz = Double.parseDouble(depth.getValue());
		}

		// **** 3. BASE PROFILE OF THE SOLID - It is a polyline
		Attribute sweptAreaAttribute = sweptSolid.getAttributeByName("SweptArea");
		if (!sweptAreaAttribute.isValid()) {
			throw new BimError("Solid attribute not found");
		}
		Entity sweptArea = service.getReferencedEntity((ReferenceAttribute) sweptAreaAttribute, revisionId);

		Double dx = new Double(0);
		Double dy = new Double(0);
		Vector3d baseProfileCentroid = new Vector3d(0, 0, 0);
		List<Vector3d> polylineVerticesAsVectors = Lists.newArrayList();
		List<Position3d> polylineVertices = Lists.newArrayList();

		if (sweptArea.getTypeName().equals("IfcRectangleProfileDef")) {
			Entity rectangleProfile = sweptArea;
			Attribute rectangleProfilePositionAttribute = rectangleProfile.getAttributeByName("Position");
			if (!rectangleProfilePositionAttribute.isValid()) {
				throw new BimError("Position attribute not found");
			}
			Entity rectangleProfilePositionEntity = service.getReferencedEntity(
					(ReferenceAttribute) rectangleProfilePositionAttribute, revisionId);

			// **** 3A. CENTRE OF RECTANGLE PROFILE with origin.z forced to 0
			Position3d rectangleProfileCentre = geomHelper.getPositionFromIfcPlacement(rectangleProfilePositionEntity);
			baseProfileCentroid = rectangleProfileCentre.getOrigin(); // We do
																		// not
																		// care
			// about its
			// reference system.

			// **** 3B. DIMENSIONS OF RECTANGLE PROFILE
			Attribute xDimAttribute = rectangleProfile.getAttributeByName("XDim");
			Attribute yDimAttribute = rectangleProfile.getAttributeByName("YDim");
			if (!xDimAttribute.isValid() || !yDimAttribute.isValid()) {
				throw new BimError("Dimension attribute not found");
			}
			dx = Double.parseDouble(xDimAttribute.getValue());
			dy = Double.parseDouble(yDimAttribute.getValue());

			// **** 3C. PROFILE VERTICES
			Vector3d v1 = new Vector3d(baseProfileCentroid.x - dx / 2, baseProfileCentroid.y - dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v1));
			Vector3d v2 = new Vector3d(baseProfileCentroid.x + dx / 2, baseProfileCentroid.y - dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v2));
			Vector3d v3 = new Vector3d(baseProfileCentroid.x + dx / 2, baseProfileCentroid.y + dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v3));
			Vector3d v4 = new Vector3d(baseProfileCentroid.x - dx / 2, baseProfileCentroid.y + dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v4));
			polylineVerticesAsVectors.add(v1);
			polylineVerticesAsVectors.add(v2);
			polylineVerticesAsVectors.add(v3);
			polylineVerticesAsVectors.add(v4);

		} else if (sweptArea.getTypeName().equals("IfcArbitraryClosedProfileDef")
				|| sweptArea.getTypeName().equals("IfcArbitraryProfileDefWithVoids")) {
			Attribute outerCurveAttribute = sweptArea.getAttributeByName("OuterCurve");
			if (!outerCurveAttribute.isValid()) {
				throw new BimError("Outer Curve attribute not found");
			}
			Entity outerCurve = service.getReferencedEntity((ReferenceAttribute) outerCurveAttribute, revisionId);
			if (!outerCurve.getTypeName().equals("IfcPolyline")) {
				throw new BimError("Curve of type " + outerCurve.getTypeName() + " not handled");
			}
			Attribute pointsAttribute = outerCurve.getAttributeByName("Points");
			if (!pointsAttribute.isValid()) {
				throw new BimError("Points attribute not found");
			}
			ListAttribute edgesOfPolylineList = (ListAttribute) pointsAttribute;
			// **** 3C. PROFILE VERTICES
			for (Attribute pointAttribute : edgesOfPolylineList.getValues()) {
				Entity edge = service.getReferencedEntity((ReferenceAttribute) pointAttribute, revisionId);
				Vector3d polylinePointCoordinates = geomHelper.getCoordinatesOfIfcCartesianPoint(edge);
				polylineVertices.add(new IfcPosition3d(polylinePointCoordinates));
				polylineVerticesAsVectors.add(polylinePointCoordinates);
			}
		} else {
			System.out.println("IfcProfileDef of type " + sweptArea.getTypeName() + " not handled");
			return null;
		}
		// **** 4. CONVERT VERTICES COORDINATES FROM THE SOLID REFERENCE SYSTEM
		convertCoordinatesAsVectors(polylineVerticesAsVectors, sweptSolidPosition);
		return new DefaultSpaceGeometry(polylineVerticesAsVectors, dz);
	}

	private void convertCoordinatesAsVectors(List<Vector3d> polylineVerticesAsVectors, Position3d sweptSolidPosition) {
		for (Vector3d vector : polylineVerticesAsVectors) {
			convertCoordinates(vector, sweptSolidPosition);
		}
	}

	private void convertCoordinates(List<Position3d> polylineVertices, Position3d sweptSolidPosition) {
		for (Position3d vertexPosition : polylineVertices) {
			convertCoordinates(vertexPosition.getOrigin(), sweptSolidPosition);
		}
	}

	private void convertCoordinates(Vector3d P, Position3d referenceSystem) {
		referenceSystem.getVersorsMatrix().transform(P);
		P.add(referenceSystem.getOrigin());
	}

	private int getIndexOfBB(Attribute representationList) {
		int index = -1;
		for (Attribute value : ((ListAttribute) representationList).getValues()) {
			index++;
			Entity representation = service.getReferencedEntity((ReferenceAttribute) value, revisionId);
			Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
			String representationType = typeAttribute.getValue();
			if (representationType.equals("BoundingBox")) {
				return index;
			}
		}
		return -1;
	}

}
