package org.cmdbuild.services.soap.security;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.auth.Login;

public class LoginAndGroup {

	public static LoginAndGroup newInstance(final Login login) {
		return new LoginAndGroup(login, null);
	}

	public static LoginAndGroup newInstance(final Login login, final String group) {
		return new LoginAndGroup(login, group);
	}

	private final Login login;
	private final String group;
	private final transient String toString;

	private LoginAndGroup(final Login login, final String group) {
		this.login = login;
		this.group = group;
		this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
				.append("login", login) //
				.append("group", group) //
				.toString();
	}

	public Login getLogin() {
		return login;
	}

	public String getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return toString;
	}

}
