package org.cmdbuild.workflow;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.List;

import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.shark.Logging;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHelper {

	private static final String CMDBUILD_API_CLASSNAME_PROPERTY = "org.cmdbuild.workflow.api.classname";
	private static final String CMDBUILD_MAIL_API_CLASSNAME_PROPERTY = "org.cmdbuild.mail.api.classname";

	private static final String MAIL_DEBUG = "DefaultMailMessageHandler.debug";
	private static final String MAIL_USE_SSL = "DefaultMailMessageHandler.useSSL";
	private static final String MAIL_SMTP_SERVER = "DefaultMailMessageHandler.SMTPMailServer";
	private static final String MAIL_SMTP_PORT = "DefaultMailMessageHandler.SMTPPortNo";
	private static final String MAIL_STARTTLS = "DefaultMailMessageHandler.starttls";
	private static final String MAIL_USE_AUTHENTICATION = "DefaultMailMessageHandler.useAuthentication";
	private static final String MAIL_USERNAME = "DefaultMailMessageHandler.Login";
	private static final String MAIL_PASSWORD = "DefaultMailMessageHandler.Password";
	private static final String MAIL_FROM_ADDRESS = "DefaultMailMessageHandler.SourceAddress";

	private static final String MAIL_MULTIPLES_SEPARATOR = ",";

	private static final String MAIL_PROTOCOL_SMTP = "smtp";
	private static final String MAIL_PROTOCOL_SMTPS = "smtps";

	private static final String NO_AUTHENTICATION_USERNAME = null;

	private final CallbackUtilities cus;

	public ConfigurationHelper(final CallbackUtilities cus) {
		this.cus = cus;
	}

	public SharkWorkflowApiFactory getWorkflowApiFactory() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final String classname = cus.getProperty(CMDBUILD_API_CLASSNAME_PROPERTY);
		cus.info(null, format("loading workflow api '%s'", classname));
		return loadClass(classname, SharkWorkflowApiFactory.class);
	}

	public MailApiFactory getMailApiFactory() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final String classname = cus.getProperty(CMDBUILD_MAIL_API_CLASSNAME_PROPERTY);
		cus.info(null, format("loading mail api '%s'", classname));
		return loadClass(classname, MailApiFactory.class);
	}

	private <T> T loadClass(final String classname, final Class<T> classToBeLoaded) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		cus.info(null, format("loading class '%s' that should be a '%s'", classname, classToBeLoaded.getName()));
		final Class<? extends T> loadedClass = Class.forName(classname).asSubclass(classToBeLoaded);
		final T instance = loadedClass.newInstance();
		return instance;
	}

	public MailApi.Configuration getMailApiConfiguration() {
		final MailApi.InputConfiguration INPUT_NOT_SUPPORTED = UnsupportedProxyFactory.of(
				MailApi.InputConfiguration.class).create();
		return new MailApi.Configuration() {

			@Override
			public boolean isDebug() {
				return Boolean.valueOf(cus.getProperty(MAIL_DEBUG));
			}

			@Override
			public Logger getLogger() {
				return LoggerFactory.getLogger(Logging.LOGGER_NAME);
			}

			@Override
			public String getOutputProtocol() {
				final boolean useSsl = Boolean.valueOf(cus.getProperty(MAIL_USE_SSL));
				return useSsl ? MAIL_PROTOCOL_SMTPS : MAIL_PROTOCOL_SMTP;
			}

			@Override
			public String getOutputHost() {
				return cus.getProperty(MAIL_SMTP_SERVER);
			}

			@Override
			public Integer getOutputPort() {
				return Integer.parseInt(cus.getProperty(MAIL_SMTP_PORT));
			}

			@Override
			public boolean isStartTlsEnabled() {
				return Boolean.valueOf(cus.getProperty(MAIL_STARTTLS));
			}

			@Override
			public String getOutputUsername() {
				final boolean useAuthentication = Boolean.valueOf(cus.getProperty(MAIL_USE_AUTHENTICATION));
				return useAuthentication ? cus.getProperty(MAIL_USERNAME) : NO_AUTHENTICATION_USERNAME;
			}

			@Override
			public String getOutputPassword() {
				return cus.getProperty(MAIL_PASSWORD);
			}

			@Override
			public List<String> getOutputFromRecipients() {
				return asList(cus.getProperty(MAIL_FROM_ADDRESS), MAIL_MULTIPLES_SEPARATOR);
			}

			@Override
			public String getInputProtocol() {
				return INPUT_NOT_SUPPORTED.getInputProtocol();
			}

			@Override
			public String getInputHost() {
				return INPUT_NOT_SUPPORTED.getInputHost();
			}

			@Override
			public Integer getInputPort() {
				return INPUT_NOT_SUPPORTED.getInputPort();
			}

			@Override
			public String getInputUsername() {
				return INPUT_NOT_SUPPORTED.getInputUsername();
			}

			@Override
			public String getInputPassword() {
				return INPUT_NOT_SUPPORTED.getInputPassword();
			}

		};
	}
}
