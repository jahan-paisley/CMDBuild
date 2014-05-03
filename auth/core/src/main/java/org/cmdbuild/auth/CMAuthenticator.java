package org.cmdbuild.auth;

import org.cmdbuild.auth.logging.LoggingSupport;
import org.slf4j.Logger;

public interface CMAuthenticator {

	Logger logger = LoggingSupport.logger;

	/**
	 * Returns the name of the authenticator, to be referenced by name.
	 * 
	 * @return the name of the authenticator
	 */
	String getName();

}
