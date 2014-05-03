package integration.mail;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.GetMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class AddressesTest extends AbstractMailTest {

	private static final String ANOTHER_FOO_AT_EXAMPLE_DOT_COM = "another_" + FOO_AT_EXAMPLE_DOT_COM;
	private static final String ANOTHER_BAR_AT_EXAMPLE_DOT_COM = "another_" + BAR_AT_EXAMPLE_DOT_COM;
	private static final String ANOTHER_BAZ_AT_EXAMPLE_DOT_COM = "another_" + BAZ_AT_EXAMPLE_DOT_COM;

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
	public void allAddresses() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withTo(collectionOf(FOO_AT_EXAMPLE_DOT_COM, ANOTHER_FOO_AT_EXAMPLE_DOT_COM)) //
				.withCc(arrayOf(BAR_AT_EXAMPLE_DOT_COM, ANOTHER_BAR_AT_EXAMPLE_DOT_COM)) //
				.withBcc(BAZ_AT_EXAMPLE_DOT_COM, ANOTHER_BAZ_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		final GetMail getMail = mailApi.selectMail(fetchedMail).get();

		// then
		assertThat(getMail.getFrom(), equalTo(FOO_AT_EXAMPLE_DOT_COM));
		assertThat(getMail.getTos(), containsInAnyOrder(FOO_AT_EXAMPLE_DOT_COM, ANOTHER_FOO_AT_EXAMPLE_DOT_COM));
		assertThat(size(getMail.getTos()), equalTo(2));
		assertThat(getMail.getCcs(), containsInAnyOrder(BAR_AT_EXAMPLE_DOT_COM, ANOTHER_BAR_AT_EXAMPLE_DOT_COM));
		assertThat(size(getMail.getCcs()), equalTo(2));
	}

	@Test
	public void addressesStrippedFromContactDetails() throws Exception {
		// given
		send(newMail(FOO, PASSWORD) //
				.withFrom(address(BAZ, BAZ_AT_EXAMPLE_DOT_COM)) //
				.withTo(address(FOO, FOO_AT_EXAMPLE_DOT_COM)) //
				.withCc(address(BAR, BAR_AT_EXAMPLE_DOT_COM)) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		final GetMail getMail = mailApi.selectMail(fetchedMail).get();

		// then
		assertThat(getMail.getFrom(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
		assertThat(getMail.getTos(), containsInAnyOrder(FOO_AT_EXAMPLE_DOT_COM));
		assertThat(getMail.getCcs(), containsInAnyOrder(BAR_AT_EXAMPLE_DOT_COM));
	}

	private String address(final String contactName, final String emailAddress) {
		return String.format("%s <%s>", contactName, emailAddress);
	}

	private Iterable<String> collectionOf(final String... elements) {
		return Arrays.asList(elements);
	}

	private String[] arrayOf(final String... elements) {
		return elements;
	}

}
