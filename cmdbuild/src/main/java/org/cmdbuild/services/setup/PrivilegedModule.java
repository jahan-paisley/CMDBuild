package org.cmdbuild.services.setup;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetupLogic.Module;

import com.google.common.collect.Maps;

public class PrivilegedModule implements Module {

	private final Module module;
	private final PrivilegeContext privilegeContext;

	public PrivilegedModule(final Module module, final PrivilegeContext privilegeContext) {
		this.module = module;
		this.privilegeContext = privilegeContext;
	}

	@Override
	public Map<String, String> retrieve() throws Exception {
		final Map<String, String> values = Maps.newHashMap();
		for (final Entry<String, String> entry : module.retrieve().entrySet()) {
			final String key = entry.getKey();
			if (privilegeContext.hasAdministratorPrivileges() || !key.endsWith("password")) {
				values.put(key, entry.getValue());
			}
		}
		return values;
	}

	@Override
	public void store(final Map<String, String> values) throws Exception {
		module.store(values);
	}

}
