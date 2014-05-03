package org.cmdbuild.services.auth;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public class UserInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Set<UserGroup> EMPTY_USERGROUPS = Collections.emptySet();

	private String username;
	private UserType userType;
	private Set<UserGroup> userGroups;

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(final UserType userType) {
		this.userType = userType;
	}

	public Set<UserGroup> getGroups() {
		final Set<UserGroup> userGroups = (this.userGroups == null) ? EMPTY_USERGROUPS : this.userGroups;
		return Collections.unmodifiableSet(userGroups);
	}

	public void setGroups(final Set<UserGroup> groups) {
		this.userGroups = groups;
	}

}
