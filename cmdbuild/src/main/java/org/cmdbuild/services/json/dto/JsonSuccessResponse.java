package org.cmdbuild.services.json.dto;

public class JsonSuccessResponse extends JsonResponse {

	protected JsonSuccessResponse(final Object response) {
		super(true, response);
	}

}
