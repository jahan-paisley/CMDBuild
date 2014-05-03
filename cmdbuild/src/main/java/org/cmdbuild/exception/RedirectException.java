package org.cmdbuild.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.logger.Log;

public class RedirectException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String url;

	public RedirectException(String url) {
		super();
		this.url = url;
	}

	public void sendRedirect(HttpServletResponse httpResponse) throws IOException {
		final String url = getUrl(httpResponse);
		Log.CMDBUILD.info("Redirecting to " + url);
		httpResponse.sendRedirect(url);
	}

	protected String getUrl(HttpServletResponse httpResponse) {
		return url;
	}
}
