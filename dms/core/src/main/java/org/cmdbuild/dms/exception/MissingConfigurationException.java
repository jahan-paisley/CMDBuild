package org.cmdbuild.dms.exception;

public class MissingConfigurationException extends DmsRuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingConfigurationException() {
		super();
	}

	public MissingConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public MissingConfigurationException(final String message) {
		super(message);
	}

	public MissingConfigurationException(final Throwable cause) {
		super(cause);
	}

}
