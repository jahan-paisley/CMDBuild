package org.cmdbuild.services.gis.geoserver.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.utils.Command;
import org.dom4j.Node;

public class ListLayers extends AbstractGeoCommand implements Command<List<LayerMetadata>> {

	/**
	 * if there is a store name, the command return only the layers of that
	 * store
	 */
	private final String storeName;

	public static List<LayerMetadata> exec(final GisConfiguration configuration) {
		return new ListLayers(configuration).run();
	}

	public static List<LayerMetadata> exec(final GisConfiguration configuration, final String storeName) {
		return new ListLayers(configuration, storeName).run();
	}

	private ListLayers(final GisConfiguration configuration) {
		this(configuration, null);
	}

	private ListLayers(final GisConfiguration configuration, final String storeName) {
		super(configuration);
		this.storeName = storeName;
	}

	@Override
	public List<LayerMetadata> run() {
		final List<LayerMetadata> layers = new ArrayList<LayerMetadata>();

		final String url = String.format("%s/rest/layers", getGeoServerURL());

		final List<?> layerList = get(url).selectNodes("//layers/layer");
		for (final Iterator<?> iter = layerList.iterator(); iter.hasNext();) {
			final String layerName = ((Node) iter.next()).valueOf("name");
			final LayerMetadata layer = GetLayer.exec(configuration, layerName);
			if (this.storeName != null && this.storeName.equals(layer.getStoreName())) {
				layers.add(layer);
			}
		}

		return layers;
	}
}
