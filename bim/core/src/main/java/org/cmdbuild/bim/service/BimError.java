package org.cmdbuild.bim.service;

@SuppressWarnings("serial")
public class BimError extends RuntimeException {

	public BimError(final Throwable e) {
		super(e);
	}

	public BimError(final String message, final Throwable e) {
		super(message, e);
	}

	public BimError(final String message) {
		super(message);
	}

}
