package org.cmdbuild.auth;

import org.apache.commons.lang3.Validate;

/**
 * Authenticates a user based on the presence of a header parameter. It can be
 * used when a Single Sign-On proxy adds the header.
 */
public class HeaderAuthenticator implements ClientRequestAuthenticator {

	public interface Configuration {
		String getHeaderAttributeName();
	}

	final Configuration conf;

	/**
	 * @param userHeader
	 *            Header that contains the user that should be logged in
	 */
	public HeaderAuthenticator(final Configuration conf) {
		Validate.notNull(conf);
		this.conf = conf;
	}

	@Override
	public String getName() {
		return "HeaderAuthenticator";
	}

	@Override
	public Response authenticate(final ClientRequest request) {
		final String loginString = request.getHeader(conf.getHeaderAttributeName());
		if (loginString != null) {
			final Login login = Login.newInstance(loginString);
			logger.info("Authenticated user " + loginString);
			return Response.newLoginResponse(login);
		} else {
			return null;
		}
	}

}
