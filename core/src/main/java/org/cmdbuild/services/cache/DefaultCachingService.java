package org.cmdbuild.services.cache;

import java.util.List;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;

public class DefaultCachingService implements CachingService {

	private static final Marker marker = MarkerFactory.getMarker(DefaultCachingService.class.getName());

	private final List<Cacheable> cacheables = Lists.newArrayList();

	public DefaultCachingService(final Iterable<Cacheable> cacheables) {
		for (final Cacheable cacheable : cacheables) {
			this.cacheables.add(cacheable);
		}
	}

	@Override
	public void clearCache() {
		for (final Cacheable cacheable : cacheables) {
			logger.debug(marker, "clearing cache for '{}'", cacheable.getClass());
			cacheable.clearCache();
		}
	}
}
