package org.cmdbuild.logic.auth;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

public class AuthenticationLogicUtils {

	private AuthenticationLogicUtils() {
		// prevents instantiation
	}

	public static boolean isLoggedIn(final HttpServletRequest request) throws RedirectException {

		final OperationUser operationUser = applicationContext().getBean(UserStore.class).getUser();
		if (operationUser == null) {
			return false;
		}
		if (operationUser.getAuthenticatedUser().isAnonymous()) {
			return false;
		}
		return operationUser.isValid();
	}

	public static void assureAdmin(final HttpServletRequest request, final AdminAccess adminAccess) {
		if (adminAccess == AdminAccess.FULL) {
			if (!isLoggedUserFullAdministrator()) {
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
			}
		} else if (adminAccess == AdminAccess.DEMOSAFE) {
			if (!isLoggedUserFullAdministrator() && !isLoggedUserDemoSafe()) {
				throw AuthExceptionType.AUTH_DEMO_MODE.createException();
			}
		}
	}

	private static boolean isLoggedUserFullAdministrator() {
		final OperationUser operationUser = applicationContext().getBean(UserStore.class).getUser();
		return operationUser != null
				&& (operationUser.hasAdministratorPrivileges() || operationUser.hasDatabaseDesignerPrivileges());
	}

	private static boolean isLoggedUserDemoSafe() {
		final OperationUser operationUser = applicationContext().getBean(UserStore.class).getUser();
		final String demoModeAdmin = CmdbuildProperties.getInstance().getDemoModeAdmin().trim();
		return demoModeAdmin.equals(StringUtils.EMPTY)
				|| demoModeAdmin.equals(operationUser.getAuthenticatedUser().getUsername());
	}

	// TODO
	private static boolean doAutoLogin() {
		// try {
		// final String username = AuthProperties.getInstance().getAutologin();
		// if (username != null && username.length() > 0) {
		// loginAs(username);
		// Log.OTHER.info("Autologin with user " + username);
		// return true;
		// }
		// } catch (final Exception e) {
		// Log.OTHER.warn("Autologin failed");
		// }
		return false;
	}

}
