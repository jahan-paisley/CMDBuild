package unit.serializers;

import junit.framework.TestCase;

import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.postgis.Geometry;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

public class TestGeoJSONSerializer extends TestCase {
	public GeoJSONSerializer serializer;

	@Override
	@Before
	public void setUp() {
		serializer = new GeoJSONSerializer();
	}

	@Test
	public void testPointSerialization() throws JSONException {
		final Geometry p = new Point(0.456, 0.654);
		final JSONObject json = serializer.serialize(p);
		final JSONObject test = new JSONObject("{type:POINT,coordinates:[0.456,0.654]}");
		assertEquals("The point isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testMultiPointSerialization() throws JSONException {
		final Point[] points = { new Point(0.1, 0.1), new Point(0.2, 0.2) };
		final MultiPoint mp = new MultiPoint(points);

		final JSONObject json = serializer.serialize(mp);
		final JSONObject test = new JSONObject("{type:MULTIPOINT,coordinates:[[0.1,0.1],[0.2,0.2]]}");

		assertEquals("The multiline isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testLineStringSerialization() throws JSONException {
		final Point[] points = { new Point(0.1, 0.1), new Point(0.2, 0.2) };
		final LineString l = new LineString(points);

		final JSONObject json = serializer.serialize(l);
		final JSONObject test = new JSONObject("{type:LINESTRING,coordinates:[[0.1,0.1],[0.2,0.2]]}");

		assertEquals("The line isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testMultiLineStringSerialization() throws JSONException {
		final Point[] points = { new Point(0.1, 0.1), new Point(0.2, 0.2) };
		final LineString[] lines = { new LineString(points), new LineString(points) };
		final MultiLineString ml = new MultiLineString(lines);

		final JSONObject json = serializer.serialize(ml);
		final JSONObject test = new JSONObject(
				"{type:MULTILINESTRING,coordinates:[[[0.1,0.1],[0.2,0.2]],[[0.1,0.1],[0.2,0.2]]]}");

		assertEquals("The multiline isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testPolygonSerialization() throws JSONException {
		final Point[] points = { new Point(0.1, 0.1), new Point(0.2, 0.2), new Point(0.3, 0.3), new Point(0.1, 0.1) };
		final LinearRing[] lr = { new LinearRing(points) };
		final Polygon pl = new Polygon(lr);

		final JSONObject json = serializer.serialize(pl);
		final JSONObject test = new JSONObject("{type:POLYGON,coordinates:[[[0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1]]]}");

		assertEquals("The polygon isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testMultyPolygonSerialization() throws JSONException {
		final Point[] points = { new Point(0.1, 0.1), new Point(0.2, 0.2), new Point(0.3, 0.3), new Point(0.1, 0.1) };
		final LinearRing[] lr = { new LinearRing(points) };
		final Polygon[] pl = { new Polygon(lr), new Polygon(lr) };
		final MultiPolygon multyPolygon = new MultiPolygon(pl);

		final JSONObject json = serializer.serialize(multyPolygon);
		final JSONObject test = new JSONObject("{type:MULTIPOLYGON," + "coordinates: [" + "["
				+ "[ [0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1] ]" + "]," + "["
				+ "[ [0.1,0.1],[0.2,0.2],[0.3,0.3],[0.1,0.1] ]" + "]" + "]}");

		assertEquals("The multipolygon isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testGeometryCollectionSerialization() throws JSONException {
		final Point point = new Point(100, 0);
		final Point[] points = { new Point(101, 0), new Point(102, 1) };
		final LineString lineString = new LineString(points);
		final Geometry[] geometries = { point, lineString };
		final GeometryCollection collection = new GeometryCollection(geometries);

		final JSONObject json = serializer.serialize(collection);
		final JSONObject test = new JSONObject("{" + " type: GEOMETRYCOLLECTION," + " geometries: [{"
				+ "   type: POINT," + "   coordinates: [100.0, 0.0]" + " },{" + "   type: LINESTRING,"
				+ "   coordinates: [ [101.0, 0.0], [102.0, 1.0] ]" + " }]" + "}");

		assertEquals("The GeometryCollection isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testGetNewFeature() throws JSONException {
		final JSONObject json = serializer.getNewFeature();
		final JSONObject test = new JSONObject("{type:Feature,geometry:{},properties:{}}");
		assertEquals("The new feature isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testGetNewFeatureCollection() throws JSONException {
		final JSONObject json = serializer.getNewFeatureCollection();
		final JSONObject test = new JSONObject("{type:FeatureCollection,features:[]}");
		assertEquals("The new featureCollections isn't well serialized: ", test.toString(), json.toString());
	}

	@Test
	public void testGetNewGeometryCollection() throws JSONException {
		final JSONObject json = serializer.getNewGeometryCollection();
		final JSONObject test = new JSONObject("{type:GeometryCollection,geometries:[]}");
		assertEquals("The new featureCollections isn't well serialized: ", test.toString(), json.toString());
	}
}
