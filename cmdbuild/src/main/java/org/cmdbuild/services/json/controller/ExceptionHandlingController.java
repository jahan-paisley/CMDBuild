package org.cmdbuild.services.json.controller;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Every controller should inherit from this class to get exception handling
 */
public abstract class ExceptionHandlingController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@ExceptionHandler(Throwable.class)
	public @ResponseBody JsonResponse exceptionHandler(final Throwable t, final HttpServletRequest request) {
		logException(t);
		return JsonResponse.failure(t);
	}

	private void logException(Throwable t) {
		logger.error("Uncatched exception", t);
	}
}
