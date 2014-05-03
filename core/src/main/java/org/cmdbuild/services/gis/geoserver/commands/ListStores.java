package org.cmdbuild.services.gis.geoserver.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreType;
import org.cmdbuild.utils.Command;
import org.dom4j.Node;

public class ListStores extends AbstractGeoCommand implements Command<List<GeoServerStore>> {

	public static List<GeoServerStore> exec(final GisConfiguration configuration) {
		return new ListStores(configuration).run();
	}

	private ListStores(final GisConfiguration configuration) {
		super(configuration);
	}

	@Override
	public List<GeoServerStore> run() {
		final List<GeoServerStore> storeList = new ArrayList<GeoServerStore>();
		for (final StoreType storeType : StoreType.values()) {
			addStoresByType(storeList, storeType);
		}
		return storeList;
	}

	private void addStoresByType(final List<GeoServerStore> storeList, final StoreType storeType) {
		final String url = String.format("%s/rest/workspaces/%s/%ss", getGeoServerURL(), getGeoServerWorkspace(),
				storeType.getName().toLowerCase());
		final String xpathExpression = String.format("//%ss/%s", storeType.getName(), storeType.getName());
		final List<?> layerList = get(url).selectNodes(xpathExpression);
		for (final Iterator<?> iter = layerList.iterator(); iter.hasNext();) {
			final String storeName = ((Node) iter.next()).valueOf("name");
			final GeoServerStore store = GetStore.exec(configuration, storeName, storeType);
			storeList.add(store);
		}
	}
}
