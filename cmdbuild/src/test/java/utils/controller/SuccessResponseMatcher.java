package utils.controller;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SuccessResponseMatcher extends TypeSafeMatcher<JsonResponse> {

	@Override
	public boolean matchesSafely(final JsonResponse response) {
		return response.isSuccess();
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("is not a success");
	}

	@Factory
	public static <T> Matcher<JsonResponse> successResponse() {
		return new SuccessResponseMatcher();
	}
}