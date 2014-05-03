package org.cmdbuild.logic.setup;

import java.util.Map;

import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;

import com.google.common.collect.Maps;

public class DefaultModulesHandler implements ModulesHandler {

	private final ModulesHandler defaultModulesHandler;
	private final Map<String, Module> modules;

	public DefaultModulesHandler(final ModulesHandler fallbackModulesHandler) {
		this.defaultModulesHandler = fallbackModulesHandler;
		this.modules = Maps.newHashMap();
	}

	public void override(final String name, final Module module) {
		modules.put(name, module);
	}

	@Override
	public Module get(final String name) {
		if (modules.containsKey(name)) {
			return modules.get(name);
		}
		return defaultModulesHandler.get(name);
	}

}
