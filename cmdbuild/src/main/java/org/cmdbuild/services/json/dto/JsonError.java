package org.cmdbuild.services.json.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.cmdbuild.exception.CMDBException;

public class JsonError {

	private String stacktrace;
	private String reason;
	private String[] parameters;

	public String getStacktrace() {
		return stacktrace;
	}

	public String getReason() {
		return reason;
	}

	public String[] getParameters() {
		return parameters;
	}

	public static JsonError fromThrowable(final Throwable t) {
		final JsonError jsonError = new JsonError();
		fillStackTrace(jsonError, t);
		fillCMDBuildExceptionData(jsonError, t);
		return jsonError;
	}

	private static void fillStackTrace(final JsonError jsonError, final Throwable t) {
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		sw.flush();
		jsonError.stacktrace = sw.toString();
	}

	private static void fillCMDBuildExceptionData(final JsonError jsonError, final Throwable t) {
		if (t instanceof CMDBException) {
			final CMDBException ce = (CMDBException) t;
			jsonError.reason = ce.getExceptionTypeText();
			jsonError.parameters = ce.getExceptionParameters();
		}
	}
}
