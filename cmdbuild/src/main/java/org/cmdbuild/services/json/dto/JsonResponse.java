package org.cmdbuild.services.json.dto;

public class JsonResponse {

	private boolean success;
	private final Object response;

	protected JsonResponse(boolean success, final Object response) {
		this.success = success;
		this.response = response;
	}

	public boolean isSuccess() {
		return success;
	}

	public Object getResponse() {
		return response;
	}

	public static JsonResponse success() {
		return success(null);
	}

	public static JsonResponse success(final Object response) {
		return new JsonSuccessResponse(response);
	}

	public static JsonResponse failure(final Throwable t) {
		return failure(null, t);
	}

	public static JsonResponse failure(final Object response, final Throwable t) {
		final JsonError error = JsonError.fromThrowable(t);
		return new JsonFailureResponse(response, error);
	}

}
