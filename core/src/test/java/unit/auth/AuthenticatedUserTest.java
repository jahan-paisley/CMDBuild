package unit.auth;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.AuthenticatedUserImpl;
import org.cmdbuild.auth.user.CMUser;
import org.junit.Test;

public class AuthenticatedUserTest {

	private final CMUser innerUser = mock(CMUser.class);

	/*
	 * Factory method
	 */

	@Test
	public void nullUserCreatesAnAnonymousUser() {
		assertThat(AuthenticatedUserImpl.newInstance(null), is(ANONYMOUS_USER));
	}

	@Test
	public void notNullUserCreatesARegularUser() {
		assertThat(AuthenticatedUserImpl.newInstance(innerUser), is(not(ANONYMOUS_USER)));
	}

	/*
	 * Anonymous user class
	 */

	@Test
	public void anonymousUserHasNoGroup() {
		assertTrue(ANONYMOUS_USER.getGroupNames().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void anonymousUserCannotSetPasswordChanger() {
		ANONYMOUS_USER.setPasswordChanger(null);
	}

	/*
	 * CMUser wrap
	 */

	@Test
	public void shouldForwardCallsToTheInnerUser() {
		when(innerUser.getUsername()).thenReturn("inner");
		when(innerUser.getDescription()).thenReturn("Inner");
		when(innerUser.getGroupNames()).thenReturn(Collections.EMPTY_SET);
		when(innerUser.getDefaultGroupName()).thenReturn("group");
		final AuthenticatedUser au = AuthenticatedUserImpl.newInstance(innerUser);

		assertThat(au.getUsername(), is("inner"));
		assertThat(au.getDescription(), is("Inner"));
		assertThat(au.getGroupNames(), is(Collections.EMPTY_SET));
		assertThat(au.getDefaultGroupName(), is("group"));
	}

	/*
	 * Password change
	 */

	@Test
	public void cannotChangePasswordIfPasswordChangerWasNotSet() {
		final AuthenticatedUser au = AuthenticatedUserImpl.newInstance(innerUser);
		assertFalse(au.canChangePassword());
		assertFalse(au.changePassword("x", "y"));
	}

	@Test
	public void canChangePasswordIfPasswordChangerWasSet() {
		final AuthenticatedUser au = AuthenticatedUserImpl.newInstance(innerUser);
		final PasswordChanger passwordChanger = mock(PasswordChanger.class);
		au.setPasswordChanger(passwordChanger);

		when(passwordChanger.changePassword(eq("x"), eq("y"))).thenReturn(Boolean.TRUE);

		assertTrue(au.canChangePassword());
		assertTrue(au.changePassword("x", "y"));

		verify(passwordChanger, only()).changePassword(eq("x"), eq("y"));
	}

}
