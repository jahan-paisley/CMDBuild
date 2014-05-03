package org.cmdbuild.services.soap.security;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.PasswordAuthenticator;

public class SoapPasswordAuthenticator implements PasswordAuthenticator {

	@Override
	public String getName() {
		return "SoapPasswordAuthenticator";
	}

	@Override
	public boolean checkPassword(final Login login, final String password) {
		return true;
	}

	@Override
	public String fetchUnencryptedPassword(final Login login) {
		return EMPTY;
	}

	@Override
	public PasswordChanger getPasswordChanger(final Login login) {
		return null;
	}

}
