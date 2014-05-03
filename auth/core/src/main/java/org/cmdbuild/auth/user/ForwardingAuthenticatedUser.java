package org.cmdbuild.auth.user;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;

public abstract class ForwardingAuthenticatedUser extends ForwardingUser implements AuthenticatedUser {

	private final AuthenticatedUser delegate;

	protected ForwardingAuthenticatedUser(final AuthenticatedUser delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public boolean isAnonymous() {
		return delegate.isAnonymous();
	}

	@Override
	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		delegate.setPasswordChanger(passwordChanger);
	}

	@Override
	public boolean changePassword(final String oldPassword, final String newPassword) {
		return delegate.changePassword(oldPassword, newPassword);
	}

	@Override
	public boolean canChangePassword() {
		return delegate.canChangePassword();
	}

}
