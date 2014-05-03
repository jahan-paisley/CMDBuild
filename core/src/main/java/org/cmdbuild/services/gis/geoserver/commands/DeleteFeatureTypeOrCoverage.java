package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.utils.Command;

public class DeleteFeatureTypeOrCoverage extends AbstractGeoCommand implements Command<Void> {

	private final LayerMetadata layer;
	private final GeoServerStore store;

	public static Void exec(final GisConfiguration configuration, final LayerMetadata layer, final GeoServerStore store) {
		return new DeleteFeatureTypeOrCoverage(configuration, layer, store).run();
	}

	private DeleteFeatureTypeOrCoverage(final GisConfiguration configuration, final LayerMetadata layer,
			final GeoServerStore store) {
		super(configuration);
		this.layer = layer;
		this.store = store;
	}

	@Override
	public Void run() {
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s/%ss/%s", getGeoServerURL(),
				getGeoServerWorkspace(), store.getStoreType().toLowerCase(), store.getName(), store.getStoreSubtype()
						.toLowerCase(), layer.getName());
		delete(url);
		return null;
	}
}
