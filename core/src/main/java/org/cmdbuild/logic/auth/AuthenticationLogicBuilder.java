package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.view.CMDataView;

public abstract class AuthenticationLogicBuilder implements Builder<AuthenticationLogic> {

	private final AuthenticationService authenticationService;
	private final PrivilegeContextFactory privilegeContextFactory;
	private final CMDataView dataView;
	private final UserStore userStore;

	protected AuthenticationLogicBuilder( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView, //
			final UserStore userStore //
	) {
		this.authenticationService = authenticationService;
		this.privilegeContextFactory = privilegeContextFactory;
		this.dataView = dataView;
		this.userStore = userStore;
	}

	@Override
	public AuthenticationLogic build() {
		return new DefaultAuthenticationLogic(authenticationService, privilegeContextFactory, dataView, userStore);
	}

}
