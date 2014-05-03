package org.cmdbuild.services.cache.wrappers;

import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class TranslationServiceWrapper implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(TranslationServiceWrapper.class.getName());

	@Override
	public void clearCache() {
		logger.info(marker, "clearing translations cache");
		TranslationService.getInstance().reload();
	}

}
