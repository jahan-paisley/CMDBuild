package org.cmdbuild.services.cache.wrappers;

import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DatabaseDriverWrapper implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(DatabaseDriverWrapper.class.getName());

	private final AbstractDBDriver driver;

	public DatabaseDriverWrapper(final AbstractDBDriver driver) {
		this.driver = driver;
	}

	@Override
	public void clearCache() {
		logger.info(marker, "clearing database driver cache");
		driver.clearCache();
	}

}
