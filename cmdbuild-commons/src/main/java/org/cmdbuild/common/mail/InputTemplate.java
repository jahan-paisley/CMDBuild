package org.cmdbuild.common.mail;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.mail.JavaxMailConstants.IMAPS;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_DEBUG;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_IMAPS_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_IMAPS_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_IMAP_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_IMAP_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_IMAP_SOCKET_FACTORY_CLASS;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_STORE_PROTOCOL;
import static org.cmdbuild.common.mail.JavaxMailConstants.NO_AUTENTICATION;
import static org.cmdbuild.common.mail.JavaxMailConstants.SSL_FACTORY;
import static org.cmdbuild.common.mail.Utils.propertiesPlusSystemOnes;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.cmdbuild.common.mail.MailApi.InputConfiguration;
import org.slf4j.Logger;

class InputTemplate {

	public static interface Hooks {

		void connected(Store store);

	}

	private final InputConfiguration configuration;
	private final Logger logger;

	public InputTemplate(final InputConfiguration configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
	}

	public void execute(final Hooks hooks) {
		Store store = null;
		try {
			final Session session = createSession();
			store = session.getStore();
			store.connect();
			hooks.connected(store);
			store.close();
		} catch (final MessagingException e) {
			logger.error("error while connecting/connected to store", e);
			throw MailException.input(e);
		} finally {
			if ((store != null) && store.isConnected()) {
				try {
					store.close();
				} catch (final MessagingException e) {
					logger.error("error while closing connection with store", e);
				}
			}
		}
	}

	private Session createSession() {
		final Properties imapProps = createConfigurationProperties();
		final Authenticator auth = getAutenticator();
		return Session.getInstance(imapProps, auth);
	}

	private Properties createConfigurationProperties() {
		final Properties properties = new Properties();
		properties.setProperty(MAIL_DEBUG, Boolean.toString(configuration.isDebug()));
		properties.setProperty(MAIL_STORE_PROTOCOL, configuration.getInputProtocol());
		if (sslRequired()) {
			properties.setProperty(MAIL_IMAPS_HOST, configuration.getInputHost());
			if (configuration.getInputPort() != null) {
				properties.setProperty(MAIL_IMAPS_PORT, configuration.getInputPort().toString());
			}
			properties.setProperty(MAIL_IMAP_SOCKET_FACTORY_CLASS, SSL_FACTORY);
		} else {
			properties.setProperty(MAIL_IMAP_HOST, configuration.getInputHost());
			if (configuration.getInputPort() != null) {
				properties.setProperty(MAIL_IMAP_PORT, configuration.getInputPort().toString());
			}
		}
		logger.trace("properties: {}", properties);
		return propertiesPlusSystemOnes(properties);
	}

	private boolean sslRequired() {
		return IMAPS.equals(configuration.getInputProtocol());
	}

	private boolean authenticationRequired() {
		return isNotBlank(configuration.getInputUsername());
	}

	private Authenticator getAutenticator() {
		return authenticationRequired() ? PasswordAuthenticator.from(configuration) : NO_AUTENTICATION;
	}

}
