package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.listeners.RequestListener;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonResponse {

	private boolean success;
	private Object response;

	public boolean isSuccess() {
		return success;
	}

	public Object getResponse() {
		return response;
	}

	public Object getWarnings() {
		final List<? extends Throwable> warnings = applicationContext().getBean(RequestListener.class) //
				.getCurrentRequest().getWarnings();
		if (warnings.size() > 0) {
			final List<JsonException> jsonWarnings = new ArrayList<JsonException>();
			for (Throwable t : warnings) {
				jsonWarnings.add(new JsonException(t));
			}
			return jsonWarnings;
		}

		return null;
	}

	public static JsonResponse success(final Object response) {
		final JsonResponse responseObject = new JsonResponse();
		responseObject.success = true;
		responseObject.response = response;
		return responseObject;
	}

	/**
	 * Use only in the JsonDispatcher, remove when switching to Spring MVC.
	 */
	public String toString() {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new JsonSerializationException(e);
		}
	}
}

class JsonException {
	private Throwable exception;

	public JsonException(Throwable exception) {
		this.exception = exception;
	}

	public String getReason() {
		String reason = null;
		if (exception instanceof CMDBException) {
			reason = ((CMDBException) exception).getExceptionTypeText();
		}

		return reason;
	}

	public String[] getReasonParameters() {
		String[] parameters = null;
		if (exception instanceof CMDBException) {
			parameters = ((CMDBException) exception).getExceptionParameters();
		}

		return parameters;
	}

	public String getStackTrace() {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		exception.printStackTrace(pw);
		sw.flush();

		return sw.toString();
	}
}

class JsonSerializationException extends RuntimeException {
	private static final long serialVersionUID = -7338020753437636167L;

	public JsonSerializationException(Exception e) {
		super(e);
	}
}
