package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.utils.Command;

public class DeleteLayer extends AbstractGeoCommand implements Command<Void> {

	private final LayerMetadata layer;

	public static Void exec(final GisConfiguration configuration, final LayerMetadata layer) {
		return new DeleteLayer(configuration, layer).run();
	}

	private DeleteLayer(final GisConfiguration configuration, final LayerMetadata layer) {
		super(configuration);
		this.layer = layer;
	}

	@Override
	public Void run() {
		final String url = String.format("%s/rest/layers/%s", getGeoServerURL(), layer.getName());
		delete(url);
		return null;
	}
}
