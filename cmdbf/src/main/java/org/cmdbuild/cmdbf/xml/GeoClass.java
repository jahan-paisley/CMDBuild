package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;

import org.cmdbuild.model.gis.LayerMetadata;

public class GeoClass extends HashMap<String, LayerMetadata> {
	private static final long serialVersionUID = 6521972156364799959L;
	private final String name;

	public GeoClass(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Iterable<? extends LayerMetadata> getLayers() {
		return values();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		return object instanceof GeoClass && name.equals(((GeoClass) object).name);
	}
}