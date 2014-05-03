package integration.mail;

import static java.util.Arrays.asList;
import integration.mail.GreenMailServer.Hooks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.Security;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApi.Configuration;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.mail.NewMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public abstract class AbstractMailTest {

	protected static final String LOCALHOST = "localhost";

	protected static final String FOO = "foo";
	protected static final String BAR = "bar";
	protected static final String BAZ = "baz";

	protected static final String PASSWORD = "s3cret";

	private static final String AT = "@";
	private static final String EXAMPLE_DOT_COM = "example.com";

	protected static final String FOO_AT_EXAMPLE_DOT_COM = FOO + AT + EXAMPLE_DOT_COM;
	protected static final String BAR_AT_EXAMPLE_DOT_COM = BAR + AT + EXAMPLE_DOT_COM;
	protected static final String BAZ_AT_EXAMPLE_DOT_COM = BAZ + AT + EXAMPLE_DOT_COM;

	protected static final String SUBJECT = "this is the subject";
	protected static final String PLAIN_TEXT_CONTENT = "this is the body";

	protected static final String MIME_TEXT_PLAIN = "text/plain";
	protected static final String MIME_TEXT_HTML = "text/html";

	private static final String NO_USERNAME = null;
	private static final String NO_PASSWORD = null;

	protected static final String INBOX = "INBOX";

	private static final String ATTACHMENT_FILE_PREFIX = "attachment";
	private static final int ATTACHMENT_BODY_PART = 1;

	protected static final Hooks SSL_HOOKS = new GreenMailServer.Hooks() {

		@Override
		public void beforeStart() {
			final String SSL_SOCKET_FACTORY_PROVIDER = "ssl.SocketFactory.provider";
			Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummySSLSocketFactory.class.getName());
		}

	};

	protected abstract ServerSetup outputServerSetup();

	protected abstract ServerSetup inputServerSetup();

	protected abstract GreenMail greenMail();

	/*
	 * Utils
	 */

	protected NewMail newMail() {
		return newMail(NO_USERNAME, NO_PASSWORD);
	}

	protected NewMail newMail(final String username, final String password) {
		final MailApiFactory mailApiFactory = new DefaultMailApiFactory();
		mailApiFactory.setConfiguration(configurationFrom(username, password));
		final MailApi mailApi = mailApiFactory.createMailApi();
		return mailApi.newMail();
	}

	protected void send(final NewMail newMail) throws Exception {
		newMail.send();
	}

	protected Configuration configurationFrom(final String username, final String password) {
		return new MailApi.Configuration() {

			@Override
			public boolean isDebug() {
				return true;
			}

			@Override
			public Logger getLogger() {
				return LoggerFactory.getLogger("TEST");
			}

			@Override
			public String getOutputProtocol() {
				return outputServerSetup().getProtocol();
			}

			@Override
			public String getOutputHost() {
				return LOCALHOST;
			}

			@Override
			public Integer getOutputPort() {
				return outputServerSetup().getPort();
			}

			@Override
			public boolean isStartTlsEnabled() {
				return false;
			}

			@Override
			public String getOutputUsername() {
				return username;
			}

			@Override
			public String getOutputPassword() {
				return password;
			}

			@Override
			public List<String> getOutputFromRecipients() {
				return asList(FOO_AT_EXAMPLE_DOT_COM);
			}

			@Override
			public String getInputProtocol() {
				return inputServerSetup().getProtocol();
			}

			@Override
			public String getInputHost() {
				return LOCALHOST;
			}

			@Override
			public Integer getInputPort() {
				return inputServerSetup().getPort();
			}

			@Override
			public String getInputUsername() {
				return username;
			}

			@Override
			public String getInputPassword() {
				return password;
			}

		};
	}

	protected MimeMessage firstReceivedMessage() {
		return greenMail().getReceivedMessages()[0];
	}

	protected URL newAttachmentFileFromContent(final String content) throws IOException {
		final File file = File.createTempFile(ATTACHMENT_FILE_PREFIX, null, FileUtils.getTempDirectory());
		file.deleteOnExit();
		FileUtils.writeStringToFile(file, content);
		return file.toURI().toURL();
	}

	protected String receivedAttachmentContent() throws IOException, MessagingException {
		final MimeMultipart mimeMultipart = MimeMultipart.class.cast(firstReceivedMessage().getContent());
		final BodyPart bodyPart = mimeMultipart.getBodyPart(ATTACHMENT_BODY_PART);
		final InputStream stream = bodyPart.getInputStream();
		final StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer);
		final String receivedAttachmentContent = writer.toString();
		return receivedAttachmentContent;
	}

}
