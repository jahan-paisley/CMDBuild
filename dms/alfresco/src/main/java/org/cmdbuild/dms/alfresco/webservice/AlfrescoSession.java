package org.cmdbuild.dms.alfresco.webservice;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DmsService.LoggingSupport;

class AlfrescoSession implements LoggingSupport {

	private final String username;
	private final String password;
	private boolean started;

	public AlfrescoSession(final String username, final String password) {
		Validate.isTrue(isNotBlank(username), format("invalid username '%s'", username));
		Validate.isTrue(isNotBlank(password), format("invalid username '%s'", password));
		this.username = username;
		this.password = password;
	}

	public synchronized void start() {
		logger.info("starting ws session");
		final String ticket = AuthenticationUtils.getTicket();
		if (ticket == null) {
			try {
				AuthenticationUtils.startSession(username, password);
				started = true;
				logger.debug("session successfully started");
			} catch (final AuthenticationFault e) {
				logger.warn("error while connecting to Alfresco", e);
				started = false;
			}
		} else {
			logger.info("session already started");
			started = true;
		}
	}

	public synchronized void end() {
		if (started) {
			logger.info("ending ws session");
			AuthenticationUtils.endSession();
			started = false;
		}
	}

	public synchronized boolean isStarted() {
		return started;
	}

}
