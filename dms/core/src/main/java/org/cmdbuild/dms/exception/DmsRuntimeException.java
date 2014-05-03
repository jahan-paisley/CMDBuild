package org.cmdbuild.dms.exception;

/**
 * Base runtime exception for DMS service.
 */
public class DmsRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected DmsRuntimeException() {
		super();
	}

	protected DmsRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	protected DmsRuntimeException(final String message) {
		super(message);
	}

	protected DmsRuntimeException(final Throwable cause) {
		super(cause);
	}

}
