package unit;

import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.DatabaseAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.dao.view.CMDataView;
import org.junit.Test;

public class LegacyDBAuthenticatorTest {

	private final CMDataView view = mock(CMDataView.class);

	@Test(expected = NullPointerException.class)
	public void viewCannotBeNull() {
		@SuppressWarnings("unused")
		final DatabaseAuthenticator authenticator = new LegacyDBAuthenticator(null);
	}

	@Test(expected = NullPointerException.class)
	public void passwordHandlerIfProvidedCannotBeNull() {
		@SuppressWarnings("unused")
		final DatabaseAuthenticator authenticator = new LegacyDBAuthenticator(view, null);
	}
}
