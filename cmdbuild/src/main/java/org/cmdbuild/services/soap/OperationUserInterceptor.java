package org.cmdbuild.services.soap;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.ForwardingAuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Iterables;

public class OperationUserInterceptor extends AbstractPhaseInterceptor<Message> implements ApplicationContextAware {

	private static final Logger logger = Log.SOAP;
	private static final Marker marker = MarkerFactory.getMarker(OperationUserInterceptor.class.getName());

	private static class AuthenticatedUserWithExtendedUsername extends ForwardingAuthenticatedUser {

		public static AuthenticatedUserWithExtendedUsername from(final AuthenticatedUser authenticatedUser,
				final String username) {
			return new AuthenticatedUserWithExtendedUsername(authenticatedUser, username);
		}

		private static final String SYSTEM = "system";
		private static final String FORMAT = "%s / %s";

		private final String username;

		private AuthenticatedUserWithExtendedUsername(final AuthenticatedUser authenticatedUser, final String username) {
			super(authenticatedUser);
			this.username = username;
		}

		@Override
		public String getUsername() {
			return String.format(FORMAT, SYSTEM, username);
		}

	}

	private static final class AuthenticatedUserWithOtherGroups extends ForwardingAuthenticatedUser {

		public static AuthenticatedUserWithOtherGroups from(final AuthenticatedUser authenticatedUser,
				final AuthenticatedUser userForGroups) {
			return new AuthenticatedUserWithOtherGroups(authenticatedUser, userForGroups);
		}

		private final AuthenticatedUser userForGroups;

		private AuthenticatedUserWithOtherGroups(final AuthenticatedUser authenticatedUser,
				final AuthenticatedUser userForGroups) {
			super(authenticatedUser);
			this.userForGroups = userForGroups;
		}

		@Override
		public Set<String> getGroupNames() {
			return userForGroups.getGroupNames();
		}

		@Override
		public List<String> getGroupDescriptions() {
			return userForGroups.getGroupDescriptions();
		}

	}

	private static Iterable<String> EMPTY_PRIVILEGED_SERVICE_USERS = Collections.emptyList();

	@Autowired
	private UserStore userStore;

	@Autowired
	private AuthenticationStore authenticationStore;

	@Autowired
	@Qualifier("soap")
	private DefaultAuthenticationService.Configuration configuration;

	private ApplicationContext applicationContext;

	public OperationUserInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(SoapAuthenticationLogicBuilder.class).build();
	}

	@Override
	public void handleMessage(final Message message) throws org.apache.cxf.interceptor.Fault {
		final String authData = new WebserviceUtils().getAuthData(message);
		final AuthenticationString authenticationString = new AuthenticationString(authData);
		storeOperationUser(authenticationString);
	}

	private void storeOperationUser(final AuthenticationString authenticationString) {
		logger.info(marker, "storing operation user for authentication string '{}'", authenticationString);
		final LoginAndGroup loginAndGroup = loginAndGroupFor(authenticationString);
		try {
			authenticationStore.setType(UserType.APPLICATION);
			tryLogin(loginAndGroup);
			authenticationStore.setLogin(loginAndGroup.getLogin());
		} catch (final RuntimeException e) {
			logger.warn(marker, "error logging in", e);
			if (authenticationString.shouldImpersonate()) {
				/*
				 * fallback to the authentication login, should always work
				 */
				final LoginAndGroup fallbackLogin = authenticationString.getAuthenticationLogin();
				userStore.setUser(null);
				tryLogin(fallbackLogin);
				authenticationStore.setLogin(loginAndGroup.getLogin());
				authenticationStore.setType(UserType.GUEST);
			} else {
				logger.error(marker, "cannot recover this error", e);
				throw e;
			}
		}
		wrapExistingOperationUser(authenticationString);
		logger.info(marker, "operation user successfully stored");
	}

	private LoginAndGroup loginAndGroupFor(final AuthenticationString authenticationString) {
		logger.debug(marker, "getting login and group for authentication string '{}'", authenticationString);
		final LoginAndGroup authenticationLogin = authenticationString.getAuthenticationLogin();
		final LoginAndGroup impersonationLogin = authenticationString.getImpersonationLogin();
		final LoginAndGroup loginAndGroup;
		if (authenticationString.shouldImpersonate()) {
			logger.debug(marker, "should authenticate");
			/*
			 * should impersonate but authentication user can be a privileged
			 * service user
			 */
			if (isPrivilegedServiceUser(authenticationLogin)) {
				/*
				 * we trust that the privileged service user has one group only
				 */
				loginAndGroup = LoginAndGroup.newInstance(authenticationLogin.getLogin());
			} else {
				loginAndGroup = impersonationLogin;
			}
		} else {
			loginAndGroup = authenticationLogin;
		}
		logger.debug(marker, "login and group are '{}'", loginAndGroup);
		return loginAndGroup;
	}

	private void wrapExistingOperationUser(final AuthenticationString authenticationString) {
		final OperationUser operationUser = userStore.getUser();
		final OperationUser wrapperOperationUser;
		if (authenticationString.shouldImpersonate()) {
			final AuthenticatedUser authenticatedUser = operationUser.getAuthenticatedUser();
			if (isPrivilegedServiceUser(authenticationString.getAuthenticationLogin())) {
				logger.debug(marker, "wrapping operation user with extended username");
				final String username = authenticationString.getImpersonationLogin().getLogin().getValue();
				wrapperOperationUser = new OperationUser( //
						AuthenticatedUserWithExtendedUsername.from(authenticatedUser, username), //
						operationUser.getPrivilegeContext(), //
						operationUser.getPreferredGroup());
			} else if (authenticationStore.getType() == UserType.DOMAIN) {
				/*
				 * we don't want that a User is represented by a Card of a user
				 * class, so we login again with the authentication user
				 * 
				 * at the end we keep the authenticated user with the privileges
				 * of the impersonated... it's a total mess
				 */
				tryLogin(authenticationString.getAuthenticationLogin());
				final OperationUser _operationUser = userStore.getUser();
				wrapperOperationUser = new OperationUser( //
						AuthenticatedUserWithOtherGroups.from(_operationUser.getAuthenticatedUser(), authenticatedUser), //
						operationUser.getPrivilegeContext(), //
						operationUser.getPreferredGroup());
				authenticationStore.setType(UserType.DOMAIN);
			} else {
				wrapperOperationUser = operationUser;
			}
		} else {
			wrapperOperationUser = operationUser;
		}
		userStore.setUser(wrapperOperationUser);
	}

	private boolean isPrivilegedServiceUser(final LoginAndGroup loginAndGroup) {
		final String username = loginAndGroup.getLogin().getValue();
		final boolean privileged = Iterables.contains(privilegedServiceUsers(), username);
		logger.debug(marker, "'{}' is {}a privileged service user", username, privileged ? EMPTY : "not");
		return privileged;
	}

	private Iterable<String> privilegedServiceUsers() {
		return (configuration.getPrivilegedServiceUsers() == null) ? EMPTY_PRIVILEGED_SERVICE_USERS : configuration
				.getPrivilegedServiceUsers();
	};

	private void tryLogin(final LoginAndGroup loginAndGroup) {
		logger.debug(marker, "trying login with '{}'", loginAndGroup);
		final Response response = authenticationLogic().login(loginFor(loginAndGroup));
		if (!response.isSuccess()) {
			// backward compatibility
			throw AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException();
		}
	}

	private LoginDTO loginFor(final LoginAndGroup loginAndGroup) {
		return LoginDTO.newInstance() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build();
	}

}
