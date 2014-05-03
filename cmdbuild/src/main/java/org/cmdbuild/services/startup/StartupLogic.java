package org.cmdbuild.services.startup;

import org.cmdbuild.logic.Logic;

/**
 * Handles all operations that needs to be performed at system startup (data
 * source migration included).
 * 
 * @since 2.2
 */
public interface StartupLogic extends Logic {

	void earlyStart();

	boolean migrationRequired();

	void migrate() throws Exception;

}
