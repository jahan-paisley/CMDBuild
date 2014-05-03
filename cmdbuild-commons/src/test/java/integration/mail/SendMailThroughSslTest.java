package integration.mail;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class SendMailThroughSslTest extends AbstractMailTest {

	@Rule
	public GreenMailServer greenMailServer = GreenMailServer.newInstance() //
			.withConfiguration(outputServerSetup()) //
			.withHooks(SSL_HOOKS) //
			.build();

	@Override
	protected ServerSetup inputServerSetup() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ServerSetup outputServerSetup() {
		return ServerSetupTest.SMTPS;
	}

	@Override
	protected GreenMail greenMail() {
		return greenMailServer.getServer();
	}

	@Test
	public void mailSendAndReceived() throws Exception {
		send(newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT));

		assertThat(greenMail().getReceivedMessages().length, equalTo(1));

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

}
