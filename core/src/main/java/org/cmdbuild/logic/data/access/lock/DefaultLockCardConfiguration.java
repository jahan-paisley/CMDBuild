package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.logic.data.access.lock.LockCardManager.LockCardConfiguration;

public class DefaultLockCardConfiguration implements LockCardConfiguration {

	private final CmdbuildConfiguration properties;

	public DefaultLockCardConfiguration(final CmdbuildConfiguration properties) {
		this.properties = properties;
	}

	@Override
	public boolean isLockerUsernameVisible() {
		return properties.getLockCardUserVisible();
	}

	@Override
	public long getExpirationTimeInMilliseconds() {
		return properties.getLockCardTimeOut() * 1000; // To have milliseconds
	}

}
