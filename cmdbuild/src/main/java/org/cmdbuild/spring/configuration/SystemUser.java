package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SYSTEM;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class SystemUser {

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private UserStore userStore;

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(SYSTEM)
	public OperationUser operationUserWithSystemPrivileges() {
		final OperationUser operationUser = userStore.getUser();
		return new OperationUser( //
				operationUser.getAuthenticatedUser(), //
				privilegeManagement.systemPrivilegeContext(), //
				operationUser.getPreferredGroup() //
		);
	}

}
