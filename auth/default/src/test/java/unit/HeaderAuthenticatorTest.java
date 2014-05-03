package unit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.ClientRequestAuthenticator;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.HeaderAuthenticator.Configuration;
import org.junit.Test;

public class HeaderAuthenticatorTest {

	private static final String USER_HEADER_NAME = "X-Username";
	private static final String USER_HEADER_VALUE = "username";
	private static final Configuration CONFIGURATION = new Configuration() {

		@Override
		public String getHeaderAttributeName() {
			return USER_HEADER_NAME;
		}
	};

	private final ClientRequest request = mock(ClientRequest.class);

	@Test(expected = NullPointerException.class)
	public void configurationCannotBeNull() {
		@SuppressWarnings("unused")
		final HeaderAuthenticator authenticator = new HeaderAuthenticator(null);
	}

	@Test
	public void doesNotAuthenticateIfTheHeaderIsNotPresent() {
		final HeaderAuthenticator authenticator = new HeaderAuthenticator(CONFIGURATION);

		final ClientRequestAuthenticator.Response response = authenticator.authenticate(request);
		assertThat(response, is(nullValue()));
	}

	@Test
	public void doesAuthenticateIfTheHeaderIsPresent() {
		final HeaderAuthenticator authenticator = new HeaderAuthenticator(CONFIGURATION);

		when(request.getHeader(USER_HEADER_NAME)).thenReturn(USER_HEADER_VALUE);

		final ClientRequestAuthenticator.Response response = authenticator.authenticate(request);
		assertThat(response.getLogin().getValue(), is(USER_HEADER_VALUE));
		assertThat(response.getRedirectUrl(), is(nullValue()));

		verify(request, only()).getHeader(USER_HEADER_NAME);
	}
}
