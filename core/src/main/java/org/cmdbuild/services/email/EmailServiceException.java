package org.cmdbuild.services.email;

import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;

public class EmailServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private EmailServiceException() {
		// prevents instantiation
	}

	public static RuntimeException receive(final Throwable cause) {
		return WorkflowExceptionType.WF_EMAIL_CANNOT_RETRIEVE_MAIL.createException();
	}

	public static RuntimeException send(final Throwable cause) {
		return WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException();
	}

}