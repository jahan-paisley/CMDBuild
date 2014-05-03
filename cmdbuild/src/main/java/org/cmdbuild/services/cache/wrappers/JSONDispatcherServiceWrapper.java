package org.cmdbuild.services.cache.wrappers;

import org.cmdbuild.services.JSONDispatcherService;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class JSONDispatcherServiceWrapper implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(JSONDispatcherServiceWrapper.class.getName());

	@Override
	public void clearCache() {
		logger.info(marker, "clearing json dispatcher's service cache");
		JSONDispatcherService.getInstance().reload();
	}

}
