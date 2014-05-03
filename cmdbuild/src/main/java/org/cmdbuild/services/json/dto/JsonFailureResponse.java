package org.cmdbuild.services.json.dto;


public class JsonFailureResponse extends JsonResponse {

	private final JsonError errors[];

	protected JsonFailureResponse(final Object response, final JsonError ... errors) {
		super(false, response);
		this.errors = errors;
	}

	public JsonError[] getErrors() {
		return errors;
	}
}
