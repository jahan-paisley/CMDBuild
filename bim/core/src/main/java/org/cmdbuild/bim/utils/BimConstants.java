package org.cmdbuild.bim.utils;

import org.apache.commons.lang3.StringUtils;

public class BimConstants {

	// IFC constants
	public static final String IFC_TYPE = "IfcType";
	public static final String IFC_GLOBALID = "GlobalId";
	public static final String IFC_DIRECTION = "IfcDirection";
	public static final String IFC_PLACEMENT_REL_TO = "PlacementRelTo";
	public static final String IFC_RELATIVE_PLACEMENT = "RelativePlacement";
	public static final String IFC_OBJECT_PLACEMENT = "ObjectPlacement";
	public static final String IFC_COORDINATES = "Coordinates";
	public static final String IFC_CARTESIAN_POINT = "IfcCartesianPoint";
	public static final String IFC_AXIS = "Axis";
	public static final String IFC_REF_DIRECTION = "RefDirection";
	public static final String IFC_AXIS2_PLACEMENT3D = "IfcAxis2Placement3D";
	public static final String IFC_AXIS2_PLACEMENT2D = "IfcAxis2Placement2D";
	public static final String IFC_DIRECTION_RATIOS = "DirectionRatios";
	public static final String IFC_REL_CONTAINED = "IfcRelContainedInSpatialStructure";
	public static final String IFC_RELATING_STRUCTURE = "RelatingStructure";
	public static final String IFC_RELATED_ELEMENTS = "RelatedElements";
	public static final String IFC_LOCATION = "Location";
	public static final String IFC_LOCAL_PLACEMENT = "IfcLocalPlacement";
	public static final String IFC_SPACE = "IfcSpace";
	public static final String IFC_BUILDING_ELEMENT_PROXY = "IfcBuildingElementProxy";
	public static final String IFC_FURNISHING = "IfcFurnishingElement";
	public static final String IFC_NAME = "Name";
	public static final String IFC_TAG = "Tag";
	public static final String IFC_DESCRIPTION = "Description";
	public static final String IFC_OBJECT_TYPE = "ObjectType";
	public static final String IFC_BUILDING_STOREY = "IfcBuildingStorey";
	public static final String IFC_BUILDING = "IfcBuilding";
	public static final String IFC_PRODUCT_DEFINITION_SHAPE = "IfcProductDefinitionShape";

	// Reader
	public static final String COORDINATES = "_Coordinates";
	public static final String GEOMETRY = "_Geometry";
	public static final String CONTAINER = "_Container";
	public static final String SPACEHEIGHT = "Height";
	public static final String SPACEGEOMETRY = "Perimeter";

	// Writer
	public static final String DEFAULT_TAG_EXPORT = "Exported from CMDB";
	public static final String OBJECT_OID = "oid";
	public static final String CONTAINER_OID = "container_oid";

	// Attributes and tables
	public static final String GLOBALID_ATTRIBUTE = IFC_GLOBALID;
	public static final String BIM_SCHEMA_NAME = "bim";
	public static final String FK_COLUMN_NAME = "Master";
	public static final String GEOMETRY_ATTRIBUTE = "Geometry";
	public static final String PERIMETER_ATTRIBUTE = "Perimeter";
	public static final String HEIGHT_ATTRIBUTE = "Height";
	public static final String PERIMETER = "Perimeter";
	public static final String HEIGHT = "Height";
	public static final String POSITION = "Position";

	public static final String X_ATTRIBUTE = "x";
	public static final String Y_ATTRIBUTE = "y";
	public static final String Z_ATTRIBUTE = "z";

	// JDBC Queries for BIM data
	public static final String INSERT_COORDINATES_QUERY_TEMPLATE = "INSERT INTO %s.\"%s\""
			+ " (\"GlobalId\", \"Geometry\",\"Master\") " + "VALUES ('%s', ST_GeomFromText('%s'), %s)";
	public static final String UPDATE_COORDINATES_QUERY_TEMPLATE = "UPDATE %s.\"%s\"" + " SET \"%s\" "
			+ "= ST_GeomFromText('%s') " + "WHERE \"%s\" = %s";

	public static final String POINT_TEMPLATE = "POINT(%s %s %s)";

	public static final String STORE_GEOMETRY_QUERY_TEMPLATE = "UPDATE %s.\"%s\"" + " SET \"%s\" "
			+ "= ST_GeomFromText('%s'), \"%s\" = %s " + "WHERE \"%s\" = %s";

	public static final String SELECT_CENTROID_QUERY_TEMPLATE = "SELECT st_x(st_centroid(\"%s\")) AS x, st_y(st_centroid(\"%s\")) AS y, "
			+ "\"%s\" AS z\n" + "FROM bim.\"%s\" \n" + "WHERE \"%s\" = %s";

	// Constants for the viewer
	public static final String ID_FIELD_NAME = "id";
	public static final String CARDID_FIELD_NAME = "id";
	public static final String CLASSID_FIELD_NAME = "classid";
	public static final String CLASSNAME_FIELD_NAME = "classname";
	public static final String CARD_DESCRIPTION_FIELD_NAME = "card_description";

	// BimServer constants
	public static final String INVALID_ID = "-1";
	
	public static boolean isValidId(final String stringId){
		return !StringUtils.isEmpty(stringId) && !stringId.equals(INVALID_ID);
	}
	

	private BimConstants() {
		throw new AssertionError();
	}
}
