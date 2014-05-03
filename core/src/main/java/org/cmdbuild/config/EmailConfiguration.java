package org.cmdbuild.config;

public interface EmailConfiguration {

	String getEmailAddress();

	String getEmailUsername();

	String getEmailPassword();

	String getSmtpServer();

	Integer getSmtpPort();

	boolean smtpNeedsSsl();

	boolean isSmtpConfigured();

	String getImapServer();

	Integer getImapPort();

	boolean isImapConfigured();

	boolean imapNeedsSsl();

	String getInputFolder();

	String getProcessedFolder();

	String getRejectedFolder();

	boolean keepUnknownMessages();

}
