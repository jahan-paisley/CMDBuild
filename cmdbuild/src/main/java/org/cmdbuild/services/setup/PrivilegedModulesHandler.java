package org.cmdbuild.services.setup;

import java.util.Set;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;

import com.google.common.collect.Sets;

public class PrivilegedModulesHandler implements ModulesHandler {

	private final ModulesHandler modulesHandler;
	private final PrivilegeContext privilegeContext;

	private final Set<String> skip;

	public PrivilegedModulesHandler(final ModulesHandler modulesHandler, final PrivilegeContext privilegeContext) {
		this.modulesHandler = modulesHandler;
		this.privilegeContext = privilegeContext;
		this.skip = Sets.newHashSet();
	}

	@Override
	public Module get(final String name) {
		final Module module = modulesHandler.get(name);
		return skip.contains(name) ? module : new PrivilegedModule(module, privilegeContext);
	}

	public void skipPrivileges(final String module) {
		skip.add(module);
	}

}
