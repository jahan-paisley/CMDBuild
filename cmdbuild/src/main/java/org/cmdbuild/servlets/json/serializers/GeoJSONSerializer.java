package org.cmdbuild.servlets.json.serializers;

import java.util.Map;
import java.util.Set;

import org.cmdbuild.logic.GISLogic.CardMapping;
import org.cmdbuild.logic.GISLogic.ClassMapping;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.Geometry;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

public class GeoJSONSerializer {

	public JSONObject serialize(final GeoFeature feature) throws JSONException {
		final JSONObject jsonGeometry = serialize(feature.getGeometry());
		final JSONObject properties = new JSONObject();
		properties.put("master_class", feature.getClassIdOfOwnerCard());
		properties.put("master_className", feature.getClassNameOfOwnerCard());
		properties.put("master_card", feature.getOwnerCardId());

		return getNewFeature(jsonGeometry, properties);
	}

	public JSONObject serialize(final Geometry geom) throws JSONException {

		final JSONObject jsonGeom = new JSONObject();
		JSONArray coordinates = new JSONArray();
		String type;

		switch (geom.getType()) {
		case Geometry.POINT: {
			final Point p = (Point) geom;
			coordinates = getJSONPointCoordinates(p);
			jsonGeom.put("coordinates", coordinates);
		}
			;
			break;
		case Geometry.MULTIPOINT: {
			final MultiPoint mp = (MultiPoint) geom;
			final Point[] points = mp.getPoints();
			for (final Point p : points) {
				coordinates.put(getJSONPointCoordinates(p));
			}
			jsonGeom.put("coordinates", coordinates);
		}
			;
			break;
		case Geometry.LINESTRING: {
			final LineString l = (LineString) geom;
			coordinates = getJSONLineCoordinates(l);
			jsonGeom.put("coordinates", coordinates);
		}
			;
			break;
		case Geometry.MULTILINESTRING: {
			final MultiLineString ml = (MultiLineString) geom;
			final LineString[] lines = ml.getLines();
			for (final LineString l : lines) {
				coordinates.put(getJSONLineCoordinates(l));
			}
			jsonGeom.put("coordinates", coordinates);
		}
			;
			break;
		case Geometry.POLYGON: {
			final Polygon polygon = (Polygon) geom;
			jsonGeom.put("coordinates", getJSONPolygonCoorditanes(polygon));
		}
			;
			break;
		case Geometry.MULTIPOLYGON: {
			final MultiPolygon multiPolygon = (MultiPolygon) geom;
			final Polygon[] polygons = multiPolygon.getPolygons();
			for (final Polygon polygon : polygons) {
				coordinates.put(getJSONPolygonCoorditanes(polygon));
			}
			jsonGeom.put("coordinates", coordinates);
		}
			;
			break;
		case Geometry.GEOMETRYCOLLECTION: {
			final GeometryCollection collection = (GeometryCollection) geom;
			final Geometry[] geometries = collection.getGeometries();
			final JSONArray jsonGeometries = new JSONArray();
			for (final Geometry geometry : geometries) {
				jsonGeometries.put(this.serialize(geometry));
			}
			jsonGeom.put("geometries", jsonGeometries);
		}
			;
			break;

		default: {
			throw new JSONException(String.format("Type %s is not supported", Geometry.getTypeString(geom.getType())));
		}
		}

		type = Geometry.getTypeString(geom.getType());
		jsonGeom.put("type", type);
		return jsonGeom;
	}

	public JSONObject getNewFeature() throws JSONException {
		return getNewFeature(new JSONObject(), new JSONObject());
	}

	public JSONObject getNewFeature(final JSONObject geometry) throws JSONException {
		return getNewFeature(geometry, new JSONObject());
	}

	public JSONObject getNewFeature(final JSONObject geometry, final JSONObject properties) throws JSONException {
		final JSONObject feature = new JSONObject();
		feature.put("type", "Feature");
		feature.put("geometry", geometry);
		feature.put("properties", properties);
		return feature;
	}

	public JSONObject getNewFeatureCollection() throws JSONException {
		return getNewFeatureCollection(new JSONArray());
	}

	public JSONObject getNewFeatureCollection(final JSONArray features) throws JSONException {
		final JSONObject featureCollection = new JSONObject();
		featureCollection.put("type", "FeatureCollection");
		featureCollection.put("features", features);
		return featureCollection;
	}

	public JSONObject getNewGeometryCollection() throws JSONException {
		return getNewGeometryCollection(new JSONArray());
	}

	public JSONObject getNewGeometryCollection(final JSONArray geometries) throws JSONException {
		final JSONObject geometry = new JSONObject();
		geometry.put("type", "GeometryCollection");
		geometry.put("geometries", geometries);
		return geometry;
	}

	public static JSONArray serializeGeoLayers(final Iterable<LayerMetadata> geoLayers) throws JSONException {
		final JSONArray jsonLayers = new JSONArray();
		for (final LayerMetadata geoLayer : geoLayers) {
			jsonLayers.put(serializeGeoLayer(geoLayer));
		}

		return jsonLayers;
	}

	public static JSONObject serializeGeoLayer(final LayerMetadata geoLayer) throws JSONException {
		final JSONObject jsonGeoLayer = new JSONObject();
		jsonGeoLayer.put("name", geoLayer.getName());
		jsonGeoLayer.put("description", geoLayer.getDescription());
		jsonGeoLayer.put("type", geoLayer.getType());
		jsonGeoLayer.put("maxZoom", geoLayer.getMaximumzoom());
		jsonGeoLayer.put("minZoom", geoLayer.getMinimumZoom());
		jsonGeoLayer.put("index", geoLayer.getIndex());
		jsonGeoLayer.put("style", geoLayer.getMapStyle());
		jsonGeoLayer.put("visibility", geoLayer.getVisibility());
		jsonGeoLayer.put("fullName", geoLayer.getFullName());
		jsonGeoLayer.put("masterTableName", geoLayer.getMasterTableName());
		jsonGeoLayer.put("geoServerName", geoLayer.getGeoServerName());
		jsonGeoLayer.put("cardBinding", serializeCardBinding(geoLayer.getCardBinding()));

		return jsonGeoLayer;
	}

	private static JSONArray serializeCardBinding(final Set<String> cardBinding) throws JSONException {
		final JSONArray out = new JSONArray();
		for (final String item : cardBinding) {
			final JSONObject jsonItem = new JSONObject();
			final String[] splittedItem = item.split("_");
			if (splittedItem.length == 2) {
				jsonItem.put("className", splittedItem[0]);
				jsonItem.put("idCard", splittedItem[1]);
			}

			out.put(jsonItem);
		}

		return out;
	}

	private JSONArray getJSONPointCoordinates(final Point p) throws JSONException {
		final JSONArray coordinates = new JSONArray();
		coordinates.put(p.getX()).put(p.getY());
		return coordinates;
	}

	private JSONArray getJSONLineCoordinates(final LineString l) throws JSONException {
		final JSONArray coordinates = new JSONArray();
		final Point[] points = l.getPoints();
		for (final Point p : points) {
			coordinates.put(getJSONPointCoordinates(p));
		}
		return coordinates;
	}

	private JSONArray getJSONRingCoordinates(final LinearRing l) throws JSONException {
		final LineString ls = new LineString(l.getPoints());
		return getJSONLineCoordinates(ls);
	}

	private JSONArray getJSONPolygonCoorditanes(final Polygon polygon) throws JSONException {
		final JSONArray rings = new JSONArray();
		int i;
		LinearRing ring;
		for (i = 0, ring = polygon.getRing(i); ring != null; ring = polygon.getRing(++i)) {
			rings.put(getJSONRingCoordinates(ring));
		}
		return rings;
	}

	public static JSONObject serialize(final Map<String, ClassMapping> geoServerLayerMapping) throws JSONException {

		final JSONObject out = new JSONObject();

		if (geoServerLayerMapping == null) {
			return out;
		}

		for (final String className : geoServerLayerMapping.keySet()) {
			final ClassMapping classMapping = geoServerLayerMapping.get(className);
			final JSONObject jsonClassMapping = new JSONObject();
			for (final String cardId : classMapping.cards()) {
				final CardMapping cardMapping = classMapping.get(cardId);
				final JSONObject jsonCardMapping = new JSONObject();
				jsonCardMapping.put("name", cardMapping.getName());
				jsonCardMapping.put("description", cardMapping.getDesription());

				jsonClassMapping.put(cardId, jsonCardMapping);
			}

			out.put(className, jsonClassMapping);
		}

		return out;
	}
}
