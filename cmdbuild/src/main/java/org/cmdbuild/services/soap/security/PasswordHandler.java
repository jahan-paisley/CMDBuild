package org.cmdbuild.services.soap.security;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationService.PasswordCallback;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * PasswordHandler class is used only with WSSecurity. This class verifies if
 * username and password in SOAP Message Header match with stored CMDBuild
 * credentials.
 */
public class PasswordHandler implements CallbackHandler {

	public static class AuthenticationString {

		private static final String PATTERN = "([^@#]+(@[^\\.]+\\.[^@#]+)?)(#([^@]+(@[^\\.]+\\.[^@]+)?))?(@([^@\\.]+))?";

		private final LoginAndGroup authenticationLogin;
		private final LoginAndGroup impersonationLogin;
		private final transient String toString;

		public AuthenticationString(final String username) {
			final Pattern pattern = Pattern.compile(PATTERN);
			final Matcher matcher = pattern.matcher(username);
			if (!matcher.find()) {
				// FIXME
				throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
			}
			final String userOrServiceUser = matcher.group(1);
			final String impersonatedUser = matcher.group(4);
			final String group = matcher.group(7);
			// final String usernameForAuthentication =
			// isNotEmpty(userNotService) ? user :
			// defaultIfBlank(userNotService,
			// user);

			authenticationLogin = LoginAndGroup.newInstance(Login.newInstance(userOrServiceUser), group);
			if (isNotEmpty(impersonatedUser)) {
				impersonationLogin = LoginAndGroup.newInstance(Login.newInstance(impersonatedUser), group);
			} else {
				impersonationLogin = null;
			}
			toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append("authentication login", authenticationLogin) //
					.append("impersonation login", impersonationLogin) //
					.toString();
		}

		public LoginAndGroup getAuthenticationLogin() {
			return authenticationLogin;
		}

		public LoginAndGroup getImpersonationLogin() {
			return impersonationLogin;
		}

		public boolean shouldImpersonate() {
			return impersonationLogin != null;
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	@Autowired
	@Qualifier("default")
	private AuthenticationService authenticationService;

	@Override
	public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (final Callback callback : callbacks) {
			if (callback instanceof WSPasswordCallback) {
				final WSPasswordCallback pwcb = (WSPasswordCallback) callback;
				final AuthenticationString wsAuthString = new AuthenticationString(pwcb.getIdentifier());
				final AuthenticatedUser user = login(pwcb, wsAuthString.getAuthenticationLogin().getLogin());
				if (user == null) {
					throw new UnsupportedCallbackException(pwcb);
				}
			}
		}
	}

	private AuthenticatedUser login(final WSPasswordCallback pwcb, final Login login)
			throws UnsupportedCallbackException {
		final AuthenticatedUser user;
		if (pwcb.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
			user = authenticationService.authenticate(login, new PasswordCallback() {
				@Override
				public void setPassword(final String password) {
					pwcb.setPassword(password);
				}
			});
		} else {
			if (AuthProperties.getInstance().getForceWSPasswordDigest()) {
				throw new UnsupportedCallbackException(pwcb, "Unsupported authentication method");
			}
			user = authenticationService.authenticate(login, pwcb.getPassword());
		}
		return user;
	}
}
