package org.cmdbuild.model.widget.service.soap.exception;

public class WebServiceException extends RuntimeException {

	public WebServiceException() {
		super();
	}

	public WebServiceException(final String message) {
		super(message);
	}

	public WebServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
