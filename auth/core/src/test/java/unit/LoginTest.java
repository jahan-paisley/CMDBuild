package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.Login.LoginType;
import org.junit.Test;

public class LoginTest {

	@Test
	public void theAtCharacterDiscriminatesBetweenEmailAndUsername() {
		final String STRING_WITHOUT_AT = "anything without the at char";
		final String STRING_WITH_AT = "anything with the @ char"; // "firstname.surname@example.com";

		final Login usernameLogin = Login.newInstance(STRING_WITHOUT_AT);
		final Login emailLogin = Login.newInstance(STRING_WITH_AT);

		assertThat(usernameLogin.getValue(), is(STRING_WITHOUT_AT));
		assertThat(usernameLogin.getType(), is(LoginType.USERNAME));

		assertThat(emailLogin.getValue(), is(STRING_WITH_AT));
		assertThat(emailLogin.getType(), is(LoginType.EMAIL));
	}

	@Test(expected = NullPointerException.class)
	public void disallowsNullLoginStrings() {
		Login.newInstance(null);
	}

	@Test
	public void implementsEqualsAndHash() {
		final Login login = Login.newInstance("A");
		final Login sameLogin = Login.newInstance("A");
		final Object anotherObject = new Object();
		final Login anotherLoginType = Login.newInstance("A", LoginType.EMAIL);
		final Login anotherLoginString = Login.newInstance("B", LoginType.USERNAME);
		final Login nullLogin = null;

		assertThat(login, equalTo(login));
		assertThat(nullLogin, not(equalTo(login)));

		assertThat(login, not(sameInstance(sameLogin)));
		assertThat(anotherObject, equalTo(anotherObject));

		assertThat(login, equalTo(sameLogin));
		assertThat(login.hashCode(), equalTo(sameLogin.hashCode()));

		assertThat(login, not(equalTo(anotherLoginType)));
		assertThat(login.hashCode(), not(equalTo(anotherLoginType.hashCode())));

		assertThat(login, not(equalTo(anotherLoginString)));
		assertThat(login.hashCode(), not(equalTo(anotherLoginString.hashCode())));
	}
}
