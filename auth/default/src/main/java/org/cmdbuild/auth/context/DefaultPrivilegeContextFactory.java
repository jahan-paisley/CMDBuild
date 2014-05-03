package org.cmdbuild.auth.context;

import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.context.DefaultPrivilegeContext.DefaultPrivilegeContextBuilder;

public class DefaultPrivilegeContextFactory implements PrivilegeContextFactory {

	/**
	 * Note: if the number of var parameters is one (the user belongs to only
	 * one group or the user selected a group) there will be only one parameter
	 * Multiple parameter only if the user has a default group (the privileges
	 * are the sum of all privileges of the groups)
	 */
	@Override
	public PrivilegeContext buildPrivilegeContext(final CMGroup... groups) {
		final DefaultPrivilegeContextBuilder privilegeCtxBuilder = DefaultPrivilegeContext.newBuilderInstance();
		for (final CMGroup group : groups) {
			final List<PrivilegePair> privileges = group.getAllPrivileges();
			privilegeCtxBuilder.withPrivileges(privileges);
		}
		return privilegeCtxBuilder.build();
	}

}
