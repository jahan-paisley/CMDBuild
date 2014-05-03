package org.cmdbuild.workflow;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.workflow.xpdl.XpdlManager.GroupQueryAdapter;

import com.google.common.base.Function;

public class DefaultGroupQueryAdapter implements GroupQueryAdapter {

	private static final Function<CMGroup, String> TO_GROUP_NAME = new Function<CMGroup, String>() {
		@Override
		public String apply(final CMGroup input) {
			return input.getName();
		}
	};

	private final AuthenticationService authenticationService;

	public DefaultGroupQueryAdapter(final AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public String[] getAllGroupNames() {
		return from(authenticationService.fetchAllGroups()) //
				.transform(TO_GROUP_NAME) //
				.toArray(String.class);
	}

}
