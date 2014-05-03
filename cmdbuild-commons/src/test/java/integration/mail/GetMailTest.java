package integration.mail;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.GetMail;
import org.cmdbuild.common.mail.GetMail.Attachment;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class GetMailTest extends AbstractMailTest {

	@Rule
	public GreenMailServer greenMailServer = GreenMailServer.newInstance() //
			.withConfiguration(inputServerSetup(), outputServerSetup()) //
			.withUser(FOO_AT_EXAMPLE_DOT_COM, FOO, PASSWORD) //
			.build();

	private MailApi mailApi;

	@Override
	protected ServerSetup inputServerSetup() {
		return ServerSetupTest.IMAP;
	}

	@Override
	protected ServerSetup outputServerSetup() {
		return ServerSetupTest.SMTP;
	}

	@Override
	protected GreenMail greenMail() {
		return greenMailServer.getServer();
	}

	@Before
	public void setUpMailApi() throws Exception {
		final MailApiFactory mailApiFactory = new DefaultMailApiFactory();
		mailApiFactory.setConfiguration(configurationFrom(FOO, PASSWORD));
		mailApi = mailApiFactory.createMailApi();
	}

	@Test
	public void simpleMail() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		final GetMail getMail = mailApi.selectMail(fetchedMail).get();

		// then
		assertThat(getMail.getId(), equalTo(fetchedMail.getId()));
		assertThat(getMail.getFolder(), equalTo(INBOX));
		assertThat(getMail.getSubject(), equalTo(SUBJECT));
		assertThat(getMail.getContent(), equalTo(PLAIN_TEXT_CONTENT));
	}

	@Test
	public void mailWithOneAttachment() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withAttachment(newAttachmentFileFromContent(BAZ)));
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		final GetMail getMail = mailApi.selectMail(fetchedMail).get();

		// then
		final Iterable<Attachment> attachments = getMail.getAttachments();
		assertThat(size(attachments), equalTo(1));
		assertThat(contentOf(get(attachments, 0)), equalTo(BAZ));
	}

	private String contentOf(final Attachment attachment) throws IOException {
		final DataHandler dataHandler = attachment.getDataHandler();
		final InputStream inputStream = dataHandler.getInputStream();
		final String content = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		return content;
	}

	@Test
	public void mailWithMultipleAttachments() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withAttachment(newAttachmentFileFromContent(FOO)) //
				.withAttachment(newAttachmentFileFromContent(BAR)) //
				.withAttachment(newAttachmentFileFromContent(BAZ)));
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		final GetMail getMail = mailApi.selectMail(fetchedMail).get();

		// then
		final Iterable<Attachment> attachments = getMail.getAttachments();
		assertThat(size(attachments), equalTo(3));
	}

	@Ignore
	@Test
	public void mailWithOneAttachmentWithNameEncodedUTF8() throws Exception {
		fail("TODO: check if the received attachment name is the same as the sent one");
	}

}
