package org.cmdbuild.auth.user;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;

public class AuthenticatedUserImpl implements AuthenticatedUser {

	public static final AuthenticatedUser ANONYMOUS_USER = new AnonymousUser();
	private final CMUser inner;
	private PasswordChanger passwordChanger;

	protected AuthenticatedUserImpl(final CMUser user) {
		Validate.notNull(user);
		this.inner = user;
	}

	public static AuthenticatedUser newInstance(final CMUser user) {
		if (user == null) {
			return ANONYMOUS_USER;
		} else {
			return new AuthenticatedUserImpl(user);
		}
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	/*
	 * Password change
	 */

	@Override
	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		this.passwordChanger = passwordChanger;
	}

	@Override
	public final boolean changePassword(final String oldPassword, final String newPassword) {
		return canChangePassword() && passwordChanger.changePassword(oldPassword, newPassword);
	}

	@Override
	public final boolean canChangePassword() {
		return passwordChanger != null;
	}

	/*
	 * CMUser
	 */

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getUsername() {
		return inner.getUsername();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public Set<String> getGroupNames() {
		return inner.getGroupNames();
	}

	@Override
	public List<String> getGroupDescriptions() {
		return inner.getGroupDescriptions();
	}

	@Override
	public String getDefaultGroupName() {
		return inner.getDefaultGroupName();
	}

	@Override
	public String getEmail() {
		return inner.getEmail();
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
	}

}
