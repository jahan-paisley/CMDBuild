package org.cmdbuild.services.gis.geoserver;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.services.gis.geoserver.commands.CreateModifyDataStore;
import org.cmdbuild.services.gis.geoserver.commands.DeleteFeatureTypeOrCoverage;
import org.cmdbuild.services.gis.geoserver.commands.DeleteLayer;
import org.cmdbuild.services.gis.geoserver.commands.DeleteStore;
import org.cmdbuild.services.gis.geoserver.commands.ListLayers;
import org.cmdbuild.services.gis.geoserver.commands.ListStores;

public class GeoServerService {

	private final GisConfiguration configuration;

	public GeoServerService(final GisConfiguration configuration) {
		this.configuration = configuration;
	}

	public List<GeoServerStore> getStores() {
		return ListStores.exec(configuration);
	}

	public List<LayerMetadata> getLayers() {
		return ListLayers.exec(configuration);
	}

	public synchronized String createStoreAndLayer(final LayerMetadata layerMetadata, final InputStream data) {
		if (nameIsNotValid(layerMetadata.getName())) {
			throw new IllegalArgumentException(String.format("Layer name must match regex \"%s\"",
					namePattern.toString()));
		}

		final GeoServerStore s = new GeoServerStore(layerMetadata.getName(), StoreDataType.valueOf(layerMetadata
				.getType()));
		return CreateModifyDataStore.exec(configuration, s, data);
	}

	public synchronized void modifyStoreData(final LayerMetadata layerMetadata, final InputStream data) {
		final GeoServerStore s = new GeoServerStore(layerMetadata.getName(), StoreDataType.valueOf(layerMetadata
				.getType()));
		CreateModifyDataStore.exec(configuration, s, data);
	}

	public synchronized void deleteStoreAndLayers(final LayerMetadata layer) {
		final StoreDataType storeDatatype = StoreDataType.valueOf(layer.getType());
		final GeoServerStore store = new GeoServerStore(layer.getName(), storeDatatype);

		try {
			// Delete the layer first because the store
			// must be empty to be deleted
			final List<LayerMetadata> storeLayers = ListLayers.exec(configuration, store.getName());
			for (final LayerMetadata geoServerLayer : storeLayers) {
				DeleteLayer.exec(configuration, geoServerLayer);
				DeleteFeatureTypeOrCoverage.exec(configuration, geoServerLayer, store);
			}
		} catch (final NotFoundException e) {
			Log.CMDBUILD.warn(String.format("GeoServer layer for store %s not found", layer.getName()));
		}

		DeleteStore.exec(configuration, store);
	}

	private static final Pattern namePattern = java.util.regex.Pattern.compile("^\\S+$");

	private boolean nameIsNotValid(final String name) {
		return !namePattern.matcher(name).matches();
	}
}
