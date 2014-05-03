package org.cmdbuild.services.cache.wrappers;

import org.cmdbuild.dms.DmsService;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DmsServiceWrapper implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(DmsServiceWrapper.class.getName());

	private final DmsService service;

	public DmsServiceWrapper(final DmsService service) {
		this.service = service;
	}

	@Override
	public void clearCache() {
		logger.info(marker, "clearing DMS cache");
		service.clearCache();
	}

}
