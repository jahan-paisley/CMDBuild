package org.cmdbuild.dms.exception;

import static java.lang.String.format;

public final class DmsError extends Exception {

	private static final long serialVersionUID = 1L;

	private DmsError(final String message) {
		super(message);
	}

	private DmsError(final Throwable cause) {
		super(cause);
	}

	public static DmsError fileNotFound(final String fileName, final String className, final String cardId) {
		return new DmsError(format("file '%s' not found for class '%s' with identifier '%d'", fileName, className,
				cardId));
	}

	public static DmsError ftpConnectionError(final String host, final String port) {
		return new DmsError(format("error connecting to '%s:%s'", host, port));
	}

	public static DmsError fptLoginError(final String username, final String password) {
		return new DmsError(format("error logging in with '%s'/'%s'", username, password));
	}

	public static DmsError ftpUploadError(final String filename) {
		return new DmsError(format("error uploading file '%s'", filename));
	}

	public static DmsError ftpDownloadError(final String filename) {
		return new DmsError(format("error downloading file '%s'", filename));
	}

	public static DmsError ftpDeleteError(final String filename) {
		return new DmsError(format("error deleting file '%s'", filename));
	}

	public static DmsError ftpOperationError(final String message) {
		return new DmsError(message);
	}

	public static DmsError wsOperationError(final String message) {
		return new DmsError(message);
	}

	public static DmsError forward(final Throwable cause) {
		return new DmsError(cause);
	}

}
