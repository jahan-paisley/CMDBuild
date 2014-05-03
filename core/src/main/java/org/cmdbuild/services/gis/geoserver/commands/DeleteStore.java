package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.utils.Command;

public class DeleteStore extends AbstractGeoCommand implements Command<Void> {

	private final GeoServerStore store;

	public static Void exec(final GisConfiguration configuration, final GeoServerStore store) {
		return new DeleteStore(configuration, store).run();
	}

	private DeleteStore(final GisConfiguration configuration, final GeoServerStore store) {
		super(configuration);
		this.store = store;
	}

	@Override
	public Void run() {
		// TODO: the documentation of GeoServer REST say that could be
		// a parameter (recurse) to remove the store and all its content
		// actually it does not work
		//
		// http://geoserver.org/display/GEOS/GSIP+58+-+RESTConfig+API+Improvements
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s", getGeoServerURL(), getGeoServerWorkspace(),
				store.getStoreType().toLowerCase(), store.getName());
		delete(url);
		return null;
	}
}
