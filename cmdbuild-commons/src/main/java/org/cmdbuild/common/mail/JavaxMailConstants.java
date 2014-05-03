package org.cmdbuild.common.mail;

class JavaxMailConstants {

	public static final String MAIL_DEBUG = "mail.debug";

	public static final String TRUE = Boolean.TRUE.toString();
	public static final String FALSE = Boolean.FALSE.toString();

	public static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	public static final String IMAPS = "imaps";
	public static final String SMTPS = "smtps";

	public static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
	public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

	public static final String MAIL_IMAP_HOST = "mail.imap.host";
	public static final String MAIL_IMAP_PORT = "mail.imap.port";

	public static final String MAIL_IMAPS_HOST = "mail.imaps.host";
	public static final String MAIL_IMAPS_PORT = "mail.imaps.port";

	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";
	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

	public static final String MAIL_SMTPS_HOST = "mail.smtps.host";
	public static final String MAIL_SMTPS_PORT = "mail.smtps.port";
	public static final String MAIL_SMTPS_AUTH = "mail.smtps.auth";

	public static final String MAIL_IMAP_SOCKET_FACTORY_CLASS = "mail.imap.socketFactory.class";
	public static final String MAIL_SMPT_SOCKET_FACTORY_CLASS = "mail.smpt.socketFactory.class";
	public static final String MAIL_SMPT_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

	public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

	public static final PasswordAuthenticator NO_AUTENTICATION = null;

	private JavaxMailConstants() {
		// prevents instantiation
	}

}
