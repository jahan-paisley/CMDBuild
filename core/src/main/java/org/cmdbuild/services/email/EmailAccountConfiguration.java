package org.cmdbuild.services.email;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.email.EmailAccount;

public class EmailAccountConfiguration implements EmailConfiguration {

	private final EmailAccount emailAccount;

	public EmailAccountConfiguration(final EmailAccount emailAccount) {
		this.emailAccount = emailAccount;
	}

	@Override
	public String getEmailAddress() {
		return emailAccount.getAddress();
	}

	@Override
	public String getEmailUsername() {
		return emailAccount.getUsername();
	}

	@Override
	public String getEmailPassword() {
		return emailAccount.getPassword();
	}

	@Override
	public String getSmtpServer() {
		return emailAccount.getSmtpServer();
	}

	@Override
	public Integer getSmtpPort() {
		return emailAccount.getSmtpPort();
	}

	@Override
	public boolean smtpNeedsSsl() {
		return emailAccount.isSmtpSsl();
	}

	@Override
	public boolean isSmtpConfigured() {
		return isNotBlank(getSmtpServer()) && isNotBlank(getEmailAddress());
	}

	@Override
	public String getImapServer() {
		return emailAccount.getImapServer();
	}

	@Override
	public Integer getImapPort() {
		return emailAccount.getImapPort();
	}

	@Override
	public boolean imapNeedsSsl() {
		return emailAccount.isImapSsl();
	}

	@Override
	public boolean isImapConfigured() {
		return isNotBlank(getImapServer()) && isNotBlank(getEmailUsername()) && isNotBlank(getEmailPassword());
	}

	@Override
	public String getInputFolder() {
		return emailAccount.getInputFolder();
	}

	@Override
	public String getProcessedFolder() {
		return emailAccount.getProcessedFolder();
	}

	@Override
	public String getRejectedFolder() {
		return emailAccount.getRejectedFolder();
	}

	@Override
	public boolean keepUnknownMessages() {
		return !emailAccount.isRejectNotMatching();
	}

}
