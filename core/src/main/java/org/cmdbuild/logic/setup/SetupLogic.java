package org.cmdbuild.logic.setup;

import java.util.Map;

import org.cmdbuild.logic.Logic;

public class SetupLogic implements Logic {

	public static interface Module {

		Map<String, String> retrieve() throws Exception;

		void store(Map<String, String> values) throws Exception;

	}

	public static interface ModulesHandler {

		Module get(String name);

	}

	private final ModulesHandler modulesHandler;

	public SetupLogic(final ModulesHandler modulesHandler) {
		this.modulesHandler = modulesHandler;
	}

	public Map<String, String> load(final String module) throws Exception {
		logger.debug("getting data for module '{}'", module);
		return modulesHandler.get(module).retrieve();
	}

	public void save(final String module, final Map<String, String> values) throws Exception {
		logger.debug("saving data for module '{}': {}", module, values);
		modulesHandler.get(module).store(values);
	}

}
