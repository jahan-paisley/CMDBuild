package org.cmdbuild.services.gis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.gis.LayerMetadata;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class GeoFeatureStore {

	private static final Marker marker = MarkerFactory.getMarker(GeoFeatureStore.class.getName());
	private static final Logger logger = LoggingSupport.sqlLogger;

	private static final String ID_CLASS_ATTRIBUTE = "IdClass";
	private static final String ID_ATTRIBUTE = "Id";
	private static final String MASTER_ATTRIBUTE = "Master";
	private static final String GEOMETRY_ATTRIBUTE = "Geometry";

	private static final String FEATURE_TABLE_ALIAS = "FeatureTable";
	private static final String OWNER_CARD_TABLE_ALIAS = "OwnerCardTable";
	private static final String MASTER_CLASS_ID_ALIAS = "MasterClassId";
	private static final String MASTER_CLASS_NAME_ALIAS = "MasterClassName";

	private static final String PROJECTION = "900913";

	private final JdbcTemplate jdbcTemplate;
	private final GisDatabaseService databaseService;

	public GeoFeatureStore( //
			final DataSource dataSource, //
			final GisDatabaseService databaseService //
	) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.databaseService = databaseService;
	}

	/* *******************************************
	 * 
	 * Manage the Geometric table
	 * 
	 * *******************************************
	 */

	private static final String GEO_TABLESPACE = "gis";
	private static final String GEO_ATTRIBUTE_TABLE_NAME_FORMAT = "Detail_%s_%s";
	private static final String GEO_TABLE_NAME_FORMAT = GEO_TABLESPACE + "." + GEO_ATTRIBUTE_TABLE_NAME_FORMAT;
	private static final String GEO_TABLE_COMMENT_TEMPLATE = "DESCR: %s|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: simpleclass"; // attribute
																																			// description
	private static final String CREATE_GEO_TABLE_TEMPLATE = "SELECT cm_create_class('%s', NULL, '%s')"; // class
																										// name,
																										// comment
	private static final String DELETE_GEO_TABLE_TEMPLATE = "SELECT cm_delete_class('%s')"; // class
																							// name
	private static final String GEO_TABLE_MASTER_ATTRIBUTE_COMMENT_TEMPLATE = "FKTARGETCLASS: %s|STATUS: active|BASEDSP: false|CLASSORDER: 0|DESCR: Master|GROUP: |INDEX: -1|MODE: write|FIELDMODE: write|NOTNULL: false|UNIQUE: false";
	private static final String GEO_TABLE_GEOMETRY_ATTRIBUTE_COMMENT_TEMPLATE = "STATUS: active|BASEDSP: false|CLASSORDER: 0|DESCR: Geometry|GROUP: |INDEX: -1|MODE: write|FIELDMODE: write|NOTNULL: false|UNIQUE: false";

	// cm_create_class_attribute(cmclass text, attributename text, sqltype text,
	// attributedefault text,
	// attributenotnull boolean, attributeunique boolean, attributecomment text)
	private static final String CREATE_ATTRIBUTE_TEMPLATE = "SELECT cm_create_class_attribute('%s','%s','%s',%s,%s,%s,'%s')";

	public String createGeoTable( //
			final String targetClassName, //
			final LayerMetadata layerMetadata) {

		final String geoAttributeTableName = String.format(GEO_TABLE_NAME_FORMAT, targetClassName,
				layerMetadata.getName());
		final String geoAttributeTableComment = String.format(GEO_TABLE_COMMENT_TEMPLATE,
				layerMetadata.getDescription());
		final String createGeoTableQuery = String.format(CREATE_GEO_TABLE_TEMPLATE, geoAttributeTableName,
				geoAttributeTableComment);
		logger.debug(marker, "Create GIS Table: {}", createGeoTableQuery);

		jdbcTemplate.query(createGeoTableQuery, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});

		// add the Master attribute
		final String masterAttribute = String.format(CREATE_ATTRIBUTE_TEMPLATE, //
				geoAttributeTableName, //
				MASTER_ATTRIBUTE, //
				"integer", //
				"NULL", //
				false, //
				false, //
				String.format(GEO_TABLE_MASTER_ATTRIBUTE_COMMENT_TEMPLATE, targetClassName) //
				);
		logger.debug(marker, "Add Master attribute to GIS Table: {}", createGeoTableQuery);
		jdbcTemplate.query(masterAttribute, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});

		// add the Geometry attribute
		final String geometryAttribute = String.format(CREATE_ATTRIBUTE_TEMPLATE, //
				geoAttributeTableName, //
				GEOMETRY_ATTRIBUTE, //
				layerMetadata.getType(), //
				"NULL", //
				false, //
				false, //
				GEO_TABLE_GEOMETRY_ATTRIBUTE_COMMENT_TEMPLATE //
				);

		logger.debug(marker, "Add Geometry attribute to GIS Table: {}", geometryAttribute);
		jdbcTemplate.query(geometryAttribute, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});

		return geoAttributeTableName;
	}

	public void deleteGeoTable(final String targetClassName, //
			final String geoAttributeName //
	) {
		final String geoAttributeTableName = String.format(GEO_TABLE_NAME_FORMAT, targetClassName, geoAttributeName);
		final String deleteGeoTableQuery = String.format(DELETE_GEO_TABLE_TEMPLATE, geoAttributeTableName);
		logger.debug(marker, "Delete GIS table: {}", deleteGeoTableQuery);

		jdbcTemplate.query(deleteGeoTableQuery, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});
	}

	/* *******************************************
	 * 
	 * Manage the features
	 * 
	 * *******************************************
	 */

	public void createGeoFeature( //
			final LayerMetadata layerMetaData, //
			final String value, //
			final Long ownerCardId) { //

		jdbcTemplate.update( //
				createGeoFeatureQuery( //
						layerMetaData, //
						value, //
						ownerCardId //
				));
	}

	public void updateGeoFeature( //
			final LayerMetadata layerMetaData, //
			final String value, //
			final Long ownerCardId) { //

		jdbcTemplate.update(updateGeoFeatureQuery( //
				layerMetaData, //
				value, //
				ownerCardId //
				) //
				);
	}

	public GeoFeature readGeoFeature( //
			final LayerMetadata layerMetaData, //
			final Card ownerCard //
	) { //

		final List<GeoFeature> geoFeature = new LinkedList<GeoFeature>();
		jdbcTemplate.query(readGeoFeatureQuery(layerMetaData, //
				ownerCard.getId() //
				), new RowCallbackHandler() {
					@Override
					public void processRow(final ResultSet rs) throws SQLException {
						final String geometryAsString = rs.getString(GEOMETRY_ATTRIBUTE);
						if (geometryAsString != null && !geometryAsString.equals("")) {
							geoFeature.add(new GeoFeature( //
									PGgeometry.geomFromString(geometryAsString), //
									ownerCard //
									));
						}
					}
				});

		if (geoFeature.size() == 0) {
			return null;
		} else {
			return geoFeature.get(0);
		}
	}

	public List<GeoFeature> readGeoFeatures( //
			final LayerMetadata layerMetaData, //
			final String bbox //
	) { //

		final List<GeoFeature> geoFeatures = new LinkedList<GeoFeature>();

		jdbcTemplate.query( //
				readGeoFeaturesQuery(layerMetaData, //
						bbox //
				), //

				new RowCallbackHandler() {
					@Override
					public void processRow(final ResultSet rs) throws SQLException {

						final String geometryAsString = rs.getString(GEOMETRY_ATTRIBUTE);
						if (geometryAsString != null && !geometryAsString.equals("")) {
							geoFeatures.add(new GeoFeature( //
									PGgeometry.geomFromString(geometryAsString), //
									rs.getLong(MASTER_ATTRIBUTE), //
									rs.getLong(MASTER_CLASS_ID_ALIAS), //
									rs.getString(MASTER_CLASS_NAME_ALIAS)));
						}
					}
				});

		return geoFeatures;
	}

	public void deleteGeoFeature(final LayerMetadata layerMetaData, final Long ownerCardId) {

		jdbcTemplate.update(deleteGeoFeatureQuery( //
				layerMetaData, //
				ownerCardId //
				));
	}

	// THE QUERIES

	private String updateGeoFeatureQuery( //
			final LayerMetadata layerMetaData, //
			final String value, //
			final Long ownerCardId //
	) {
		final String format = "UPDATE %s SET \"%s\" = ST_GeomFromText('%s',900913) WHERE \"%s\" = %s";
		final String updateFeatureQuery = String.format(format, //
				geoAttributeTable(layerMetaData), //
				GEOMETRY_ATTRIBUTE, //
				value, //
				MASTER_ATTRIBUTE, //
				ownerCardId //
				);

		logger.debug(marker, "Update geo feature query: {}", updateFeatureQuery);
		return updateFeatureQuery;
	}

	private String createGeoFeatureQuery(final LayerMetadata layerMetaData, //
			final String value, //
			final Long ownerCardId) {

		final String template = "INSERT INTO %s (\"%s\", \"%s\") VALUES (%s, ST_GeomFromText('%s',%s))";
		final String createFeatureQuery = String.format( //
				template, //
				geoAttributeTable(layerMetaData), //
				MASTER_ATTRIBUTE, //
				GEOMETRY_ATTRIBUTE, //
				ownerCardId, //
				value, //
				PROJECTION //
				);

		logger.debug(marker, "Create geo feature query: {}", createFeatureQuery);
		return createFeatureQuery;
	}

	private String readGeoFeatureQuery(final LayerMetadata layerMetaData, //
			final Long ownerCardId //
	) { //

		final String template = "SELECT \"%s\" FROM %s WHERE \"%s\" = %s";
		final String readFeatureQuery = String.format( //
				template, //
				GEOMETRY_ATTRIBUTE, //
				geoAttributeTable(layerMetaData), //
				MASTER_ATTRIBUTE, //
				ownerCardId //
				);

		logger.debug(marker, "Read single feature query: {}", readFeatureQuery);
		return readFeatureQuery;
	}

	private String deleteGeoFeatureQuery(final LayerMetadata layerMetaData, final Long ownerCardId) {

		final String template = "DELETE FROM %s WHERE \"%s\" = %s";
		final String deleteGeoFeatureQuery = String.format( //
				template, //
				geoAttributeTable(layerMetaData), //
				MASTER_ATTRIBUTE, //
				ownerCardId //
				);

		logger.debug(marker, "Delete feature query: {}", deleteGeoFeatureQuery);
		return deleteGeoFeatureQuery;
	}

	private String geoAttributeTable(final LayerMetadata layerMetadata) {
		final String template = "\"%s\".\"%s\"";
		final String[] geoFeatureNameParts = layerMetadata.getFullName().split("\\.");
		return String.format(template, geoFeatureNameParts[0], geoFeatureNameParts[1]);
	}

	private String readGeoFeaturesQuery(final LayerMetadata layerMetaData, //
			final String bbox //
	) {

		final String readGeoFeaturesQuery = String.format("%s %s %s", //
				readGeoFeatureQuerySelectPart(), //
				readGeoFeatureQueryFromPart(layerMetaData), //
				getGeoFeaturesQueryWherePart(bbox) //
				);

		logger.debug(marker, "Read features query: {}", readGeoFeaturesQuery);
		return readGeoFeaturesQuery;
	}

	private String readGeoFeatureQuerySelectPart() {
		final String selectTemplate = "SELECT \"%s\".\"%s\", " + "asText(\"%s\".\"%s\") AS \"%s\", "
				+ "\"%s\".\"%s\"::oid AS \"%s\", " + "_cm_cmtable(\"%s\".\"%s\"::oid) AS \"%s\"";

		return String.format(selectTemplate, //
				FEATURE_TABLE_ALIAS, //
				MASTER_ATTRIBUTE, //
				FEATURE_TABLE_ALIAS, //
				GEOMETRY_ATTRIBUTE, //
				GEOMETRY_ATTRIBUTE, //
				OWNER_CARD_TABLE_ALIAS, //
				ID_CLASS_ATTRIBUTE, //
				MASTER_CLASS_ID_ALIAS, //
				OWNER_CARD_TABLE_ALIAS, //
				ID_CLASS_ATTRIBUTE, //
				MASTER_CLASS_NAME_ALIAS //
				);
	}

	private String readGeoFeatureQueryFromPart(final LayerMetadata layerMetadata) {
		final String fromTemplate = "FROM %s AS \"%s\" " + "JOIN \"%s\" AS \"%s\" "
				+ "ON \"%s\".\"%s\" = \"%s\".\"%s\" ";

		return String.format(fromTemplate, //
				geoAttributeTable(layerMetadata), //
				FEATURE_TABLE_ALIAS, //
				layerMetadata.getMasterTableName(), //
				OWNER_CARD_TABLE_ALIAS, //
				FEATURE_TABLE_ALIAS, //
				MASTER_ATTRIBUTE, //
				OWNER_CARD_TABLE_ALIAS, ID_ATTRIBUTE //
				);
	}

	/**
	 * Check the version of postGIS because from 2.0.0 the function to set a
	 * SRID changed the signature
	 * 
	 * @return the string format for the bbox filter
	 */
	private String getGeoFeaturesQueryWherePart(final String bbox) {
		if ("".equals(bbox)) {
			return " WHERE TRUE";
		}

		final String postGISVersion = databaseService.getPostGISVersion();
		final String whereTemplate = " WHERE (\"%s\".\"%s\" && %s('BOX3D(%s)'::box3d,%s))";
		String postgisFuntion = "ST_SetSRID";

		if (postGISVersion != null) {
			if ("2.0.0".compareTo(postGISVersion) >= 0) {
				postgisFuntion = "SetSRID";
			}
		}

		return String.format(whereTemplate, //
				FEATURE_TABLE_ALIAS, //
				GEOMETRY_ATTRIBUTE, //
				postgisFuntion, //
				clearBBoxString(bbox), //
				PROJECTION //
				);
	}

	/*
	 * convert the BBox from the open-layer format to the PostGis format: from
	 * (aa.aa,bb.bb,cc.cc,dd.dd) to (aa.aa bb.bb, cc.cc dd.dd)
	 */
	private String clearBBoxString(final String bbox) {
		final String[] coordinates = bbox.split(",");
		final String cleanedBBox = String.format("%s %s, %s %s", (Object[]) coordinates);
		return cleanedBBox;
	}
}
