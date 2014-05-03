package org.cmdbuild.logic;

import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.slf4j.Logger;

public class PrivilegeUtils {

	private static final Logger logger = Logic.logger;

	private PrivilegeUtils() {
		// prevents instantiation
	}

	/**
	 * Usable with {@link OperationUser#has...Privileges()} and so on. Needed
	 * for centralize {@link AuthExceptionType#AUTH_NOT_AUTHORIZED} exception.
	 */
	public static void assure(final boolean condition) {
		assure(condition, null);
	}

	/**
	 * See {@link PrivilegeUtils#assure(boolean)}.
	 */
	public static void assure(final boolean condition, final String logMessage) {
		if (!condition) {
			if (logMessage != null) {
				logger.error(logMessage);
			}
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

}
