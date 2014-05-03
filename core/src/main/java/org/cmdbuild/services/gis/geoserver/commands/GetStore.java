package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreType;
import org.cmdbuild.utils.Command;
import org.dom4j.Document;

public class GetStore extends AbstractGeoCommand implements Command<GeoServerStore> {

	private final String name;
	private final StoreType type;

	public static GeoServerStore exec(final GisConfiguration configuration, final String name, final StoreType type) {
		return new GetStore(configuration, name, type).run();
	}

	private GetStore(final GisConfiguration configuration, final String name, final StoreType type) {
		super(configuration);
		this.name = name;
		this.type = type;
	}

	@Override
	public GeoServerStore run() {
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s", getGeoServerURL(), getGeoServerWorkspace(),
				type.getName().toLowerCase(), name);
		final Document xmlLayer = get(url);
		final StoreDataType dataType = extractDataType(xmlLayer);
		return new GeoServerStore(name, dataType);
	}

	private StoreDataType extractDataType(final Document xmlLayer) {
		String dataTypeName;
		try {
			final String xpathExpression = String.format("//%s/type", type.getName());
			dataTypeName = xmlLayer.valueOf(xpathExpression);
		} catch (final Exception e) {
			dataTypeName = null;
		}
		return getStoreDataTypeBySubtype(dataTypeName);
	}

	private StoreDataType getStoreDataTypeBySubtype(final String subtype) {
		for (final StoreDataType dt : StoreDataType.values()) {
			if (dt.getStoreSubtype().equals(subtype)) {
				return dt;
			}
		}
		return StoreDataType.SHAPE;
	}
}
