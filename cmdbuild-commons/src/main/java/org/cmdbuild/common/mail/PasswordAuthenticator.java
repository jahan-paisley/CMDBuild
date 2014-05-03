package org.cmdbuild.common.mail;

import javax.mail.PasswordAuthentication;

import org.cmdbuild.common.mail.MailApi.InputConfiguration;
import org.cmdbuild.common.mail.MailApi.OutputConfiguration;

class PasswordAuthenticator extends javax.mail.Authenticator {

	private final PasswordAuthentication authentication;

	public PasswordAuthenticator(final String username, final String password) {
		authentication = new PasswordAuthentication(username, password);
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return authentication;
	}

	public static PasswordAuthenticator from(final InputConfiguration configuration) {
		return new PasswordAuthenticator(configuration.getInputUsername(), configuration.getInputPassword());
	}

	public static PasswordAuthenticator from(final OutputConfiguration configuration) {
		return new PasswordAuthenticator(configuration.getOutputUsername(), configuration.getOutputPassword());
	}

}
