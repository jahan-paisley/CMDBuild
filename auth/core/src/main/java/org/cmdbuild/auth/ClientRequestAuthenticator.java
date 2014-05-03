package org.cmdbuild.auth;

public interface ClientRequestAuthenticator extends CMAuthenticator {

	interface ClientRequest {

		String getRequestUrl();

		String getHeader(String name);

		String getParameter(String name);
	}

	static class Response {
		private final Login login;
		private final String redirectUrl;

		public static Response newLoginResponse(final Login login) {
			return new Response(login, null);
		}

		public static Response newRedirectResponse(final String redirectUrl) {
			return new Response(null, redirectUrl);
		}

		private Response(final Login login, final String redirectUrl) {
			this.login = login;
			this.redirectUrl = redirectUrl;
		}

		public final Login getLogin() {
			return login;
		}

		public final String getRedirectUrl() {
			return redirectUrl;
		}
	}

	/**
	 * 
	 * @param request
	 * @return null if it was not authenticated and it does not require further
	 *         actions
	 */
	Response authenticate(ClientRequest request);
}
