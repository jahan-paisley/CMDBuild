package org.cmdbuild.common.mail;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.mail.JavaxMailConstants.CONTENT_TYPE_TEXT_PLAIN;
import static org.cmdbuild.common.mail.JavaxMailConstants.FALSE;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_DEBUG;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMPT_SOCKET_FACTORY_CLASS;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMPT_SOCKET_FACTORY_FALLBACK;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_AUTH;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTPS_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_AUTH;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_HOST;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_PORT;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_SMTP_STARTTLS_ENABLE;
import static org.cmdbuild.common.mail.JavaxMailConstants.MAIL_TRANSPORT_PROTOCOL;
import static org.cmdbuild.common.mail.JavaxMailConstants.NO_AUTENTICATION;
import static org.cmdbuild.common.mail.JavaxMailConstants.SMTPS;
import static org.cmdbuild.common.mail.JavaxMailConstants.SSL_FACTORY;
import static org.cmdbuild.common.mail.JavaxMailConstants.TRUE;
import static org.cmdbuild.common.mail.Utils.propertiesPlusSystemOnes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.cmdbuild.common.mail.MailApi.OutputConfiguration;
import org.slf4j.Logger;

class DefaultNewMail implements NewMail {

	private static String MULTIPART_TYPE_WHEN_ATTACHMENTS = "mixed";

	private final OutputConfiguration configuration;
	private final Logger logger;

	private final List<String> froms;
	private final Map<RecipientType, Set<String>> recipients;
	private String subject;
	private String body;
	private String contentType;
	private final Map<URL, String> attachments;
	private boolean asynchronous;

	private Message message;

	public DefaultNewMail(final OutputConfiguration configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();

		this.froms = new ArrayList<String>();

		this.recipients = new HashMap<RecipientType, Set<String>>();
		recipients.put(RecipientType.TO, new HashSet<String>());
		recipients.put(RecipientType.CC, new HashSet<String>());
		recipients.put(RecipientType.BCC, new HashSet<String>());

		contentType = CONTENT_TYPE_TEXT_PLAIN;

		attachments = new HashMap<URL, String>();
	}

	@Override
	public NewMail withFrom(final String from) {
		froms.add(from);
		return this;
	}

	@Override
	public NewMail withTo(final String to) {
		addRecipient(RecipientType.TO, to);
		return this;
	}

	@Override
	public NewMail withTo(final String... tos) {
		return withTo(Arrays.asList(tos));
	}

	@Override
	public NewMail withTo(final Iterable<String> tos) {
		addRecipients(RecipientType.TO, tos);
		return this;
	}

	@Override
	public NewMail withCc(final String cc) {
		addRecipient(RecipientType.CC, cc);
		return this;
	}

	@Override
	public NewMail withCc(final String... ccs) {
		return withCc(Arrays.asList(ccs));
	}

	@Override
	public NewMail withCc(final Iterable<String> ccs) {
		addRecipients(RecipientType.CC, ccs);
		return this;
	}

	@Override
	public NewMail withBcc(final String bcc) {
		addRecipient(RecipientType.BCC, bcc);
		return this;
	}

	@Override
	public NewMail withBcc(final String... bccs) {
		return withBcc(Arrays.asList(bccs));
	}

	@Override
	public NewMail withBcc(final Iterable<String> bccs) {
		addRecipients(RecipientType.BCC, bccs);
		return this;
	}

	private void addRecipient(final RecipientType type, final String recipient) {
		if (isBlank(recipient)) {
			logger.info("invalid recipient {} '{}', will not be added", type.getClass().getSimpleName(), recipient);
		} else {
			recipients.get(type).add(recipient);
		}
	}

	private void addRecipients(final RecipientType type, final Iterable<String> recipients) {
		if (recipients != null) {
			for (final String recipient : recipients) {
				addRecipient(type, recipient);
			}
		}
	}

	@Override
	public NewMail withSubject(final String subject) {
		this.subject = subject;
		return this;
	}

	@Override
	public NewMail withContent(final String body) {
		this.body = body;
		return this;
	}

	@Override
	public NewMail withContentType(final String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public NewMail withAttachment(final URL url) {
		return withAttachment(url, null);
	}

	@Override
	public NewMail withAttachment(final URL url, final String name) {
		attachments.put(url, name);
		return this;
	}

	@Override
	public NewMail withAttachment(final String url) {
		return withAttachment(url, null);
	}

	@Override
	public NewMail withAttachment(final String url, final String name) {
		try {
			final URL realUrl = new URL(url);
			attachments.put(realUrl, name);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	@Override
	public NewMail withAsynchronousSend(final boolean asynchronous) {
		this.asynchronous = asynchronous;
		return this;
	}

	@Override
	public void send() {
		final Runnable job = sendJob();
		if (asynchronous) {
			runInAnotherThread(job);
		} else {
			job.run();
		}
	}

	private Runnable sendJob() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					final Session session = createSession();
					message = messageFrom(session);
					setFrom();
					addRecipients();
					setSubject();
					setSentDate();
					setBody();
					send(session);
				} catch (final MessagingException e) {
					logger.error("error sending mail", e);
				}
			}

		};
	}

	private void runInAnotherThread(final Runnable job) {
		new Thread(job).start();
	}

	private Session createSession() {
		final Properties properties = createConfigurationProperties();
		final Authenticator authenticator = getAutenticator();
		return Session.getInstance(properties, authenticator);
	}

	private MimeMessage messageFrom(final Session session) {
		return new MimeMessage(session);
	}

	private Properties createConfigurationProperties() {
		final Properties properties = System.getProperties();
		properties.setProperty(MAIL_DEBUG, Boolean.toString(configuration.isDebug()));
		properties.setProperty(MAIL_TRANSPORT_PROTOCOL, configuration.getOutputProtocol());
		properties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, configuration.isStartTlsEnabled() ? TRUE : FALSE);
		final String auth = authenticationRequired() ? TRUE : FALSE;
		if (sslRequired()) {
			properties.setProperty(MAIL_SMTPS_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTPS_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTPS_AUTH, auth);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_CLASS, SSL_FACTORY);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_FALLBACK, FALSE);
		} else {
			properties.setProperty(MAIL_SMTP_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTP_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTP_AUTH, auth);
		}
		logger.trace("properties: {}", properties);
		return propertiesPlusSystemOnes(properties);
	}

	private boolean sslRequired() {
		return SMTPS.equals(configuration.getOutputProtocol());
	}

	private boolean authenticationRequired() {
		return isNotBlank(configuration.getOutputUsername());
	}

	private Authenticator getAutenticator() {
		return authenticationRequired() ? PasswordAuthenticator.from(configuration) : NO_AUTENTICATION;
	}

	private void setFrom() throws MessagingException {
		final List<Address> addresses = new ArrayList<Address>();
		final List<String> fromsSource = froms.isEmpty() ? configuration.getOutputFromRecipients() : froms;
		for (final String address : fromsSource) {
			addresses.add(new InternetAddress(address));
		}
		message.addFrom(addresses.toArray(new Address[addresses.size()]));
	}

	private void addRecipients() throws MessagingException {
		for (final RecipientType type : asList(RecipientType.TO, RecipientType.CC, RecipientType.BCC)) {
			for (final String recipient : recipients.get(type)) {
				final Address address = new InternetAddress(recipient);
				message.addRecipient(type, address);
			}
		}
	}

	private void setSubject() throws MessagingException {
		message.setSubject(subject);
	}

	private void setSentDate() throws MessagingException {
		message.setSentDate(Calendar.getInstance().getTime());
	}

	private void setBody() throws MessagingException {
		final Part part;
		if ((isBlank(contentType) || CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) && !hasAttachments()) {
			part = message;
			part.setText(defaultString(body));
		} else {
			final Multipart mp = new MimeMultipart(MULTIPART_TYPE_WHEN_ATTACHMENTS);
			part = new MimeBodyPart();
			part.setContent(defaultString(body), contentType);
			mp.addBodyPart((MimeBodyPart) part);
			if (hasAttachments()) {
				addAttachmentBodyParts(mp);
			}
			message.setContent(mp);
		}

	}

	private boolean hasAttachments() {
		return !attachments.isEmpty();
	}

	private void addAttachmentBodyParts(final Multipart multipart) throws MessagingException {
		for (final Entry<URL, String> attachment : attachments.entrySet()) {
			final BodyPart bodyPart = getBodyPartFor(attachment.getKey(), attachment.getValue());
			multipart.addBodyPart(bodyPart);
		}
	}

	private BodyPart getBodyPartFor(final URL file, final String name) throws MessagingException {
		final BodyPart bodyPart = new MimeBodyPart();
		final DataSource source = new URLDataSource(file);
		bodyPart.setDataHandler(new DataHandler(source));
		bodyPart.setFileName((name == null) ? getFileName(file.getFile()) : name);
		return bodyPart;
	}

	private String getFileName(String name) {
		final String[] dirs = name.split("/");
		if (dirs.length > 0) {
			name = dirs[dirs.length - 1];
		}
		return name;
	}

	private void send(final Session session) throws MessagingException {
		Transport transport = null;
		try {
			transport = connect(session);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (final MessagingException e) {
			throw e;
		} finally {
			closeIfOpened(transport);
		}
	}

	private Transport connect(final Session session) throws MessagingException {
		final Transport transport = session.getTransport();
		transport.connect();
		return transport;
	}

	private void closeIfOpened(final Transport transport) {
		if (transport != null && transport.isConnected()) {
			try {
				transport.close();
			} catch (final MessagingException e) {
				logger.warn("error closing transport, ignoring it", e);
			}
		}
	}

}
