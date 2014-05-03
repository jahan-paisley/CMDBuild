package unit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.CasAuthenticator.CasService;
import org.cmdbuild.auth.CasAuthenticator.Configuration;
import org.cmdbuild.auth.ClientRequestAuthenticator;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.junit.Ignore;
import org.junit.Test;

public class CasAuthenticatorTest {

	private static final String REQUEST_URL = "http://cmdbuild.example.com/";

	private final ClientRequest request = mock(ClientRequest.class);
	private final CasService casService = mock(CasService.class);

	@Test(expected = NullPointerException.class)
	public void casConfigurationCannotBeNull() {
		@SuppressWarnings("unused")
		final CasAuthenticator authenticator = new CasAuthenticator((Configuration) null);
	}

	@Test
	public void doesAuthenticateIfTheTokenIsCorrect() {
		final CasAuthenticator authenticator = new CasAuthenticator(casService);
		final String userForTicket = "i am a user";

		when(casService.getUsernameFromTicket(any(ClientRequest.class))).thenReturn(userForTicket);

		final ClientRequestAuthenticator.Response response = authenticator.authenticate(request);

		assertThat(response.getLogin().getValue(), is(userForTicket));
		assertThat(response.getRedirectUrl(), is(nullValue()));
	}

	@Test
	public void redirectToCasServerIfNoCasToken() {
		final CasAuthenticator authenticator = new CasAuthenticator(casService);
		final String redirectUrl = "just a fake redirect url";

		when(request.getRequestUrl()).thenReturn(REQUEST_URL);
		when(casService.getRedirectUrl(any(ClientRequest.class))).thenReturn(redirectUrl);

		final ClientRequestAuthenticator.Response response = authenticator.authenticate(request);

		assertThat(response.getLogin(), is(nullValue()));
		assertThat(response.getRedirectUrl(), is(redirectUrl));
	}

	@Ignore
	@Test
	public void redirectToCasServerContainsSkipSso() {
		// How to test it?
	}

	@Test
	public void triesAuthenticationButDoesNotRedirectIfSkipSsoParameterPresent() {
		final CasAuthenticator authenticator = new CasAuthenticator(casService);

		when(request.getParameter(CasAuthenticator.SKIP_SSO_PARAM)).thenReturn(StringUtils.EMPTY);

		authenticator.authenticate(request);

		verify(casService, only()).getUsernameFromTicket(any(ClientRequest.class));
	}

}
