package org.cmdbuild.services.startup;

import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.services.PatchManager;

public class DefaultStartupLogic implements StartupLogic {

	private final StartupManager startupManager;
	private final PatchManager patchManager;
	private final CachingLogic cachingLogic;

	public DefaultStartupLogic(final StartupManager startupManager, final PatchManager patchManager,
			final CachingLogic cachingLogic) {
		this.startupManager = startupManager;
		this.patchManager = patchManager;
		this.cachingLogic = cachingLogic;
	}

	@Override
	public void earlyStart() {
		startupManager.start();
	}

	@Override
	public boolean migrationRequired() {
		return !patchManager.isUpdated();
	}

	@Override
	public void migrate() throws Exception {
		patchManager.applyPatchList();
		startupManager.start();
		cachingLogic.clearCache();
	}

}
