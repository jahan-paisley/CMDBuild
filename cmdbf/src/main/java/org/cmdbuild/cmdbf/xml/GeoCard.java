package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;
import java.util.Map;

import org.postgis.Geometry;

public class GeoCard {
	private final GeoClass type;
	private final Map<String, Geometry> geometries;

	public GeoCard(final GeoClass type) {
		this.type = type;
		geometries = new HashMap<String, Geometry>();
	}

	public GeoClass getType() {
		return type;
	}

	public Geometry get(final String name) {
		return geometries.get(name);
	}

	public void set(final String name, final Geometry geometry) {
		geometries.put(name, geometry);
	}

	public boolean isEmpty() {
		return geometries.isEmpty();
	}
}
