package integration.mail;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class FetchMailTest extends AbstractMailTest {

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
	public void noMailFetched() throws Exception {
		// when
		final Iterable<FetchedMail> fetched = mailApi.selectFolder(INBOX) //
				.fetch();

		// then
		assertThat(size(fetched), equalTo(0));
	}

	@Test
	public void allMailsFetched() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		// when
		final Iterable<FetchedMail> fetched = mailApi.selectFolder(INBOX) //
				.fetch();

		// then
		assertThat(size(fetched), equalTo(3));
	}

	@Test
	public void mailFetched() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		// when
		final Iterable<FetchedMail> fetched = mailApi.selectFolder(INBOX) //
				.fetch();

		// then
		assertThat(size(fetched), equalTo(1));
		final FetchedMail fetchedMail = get(fetched, 0);
		assertThat(fetchedMail.getFolder(), equalTo(INBOX));
		assertThat(fetchedMail.getSubject(), equalTo(SUBJECT));
	}

}
