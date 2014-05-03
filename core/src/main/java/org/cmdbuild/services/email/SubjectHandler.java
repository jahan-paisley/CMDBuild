package org.cmdbuild.services.email;

import org.cmdbuild.model.email.Email;

/**
 * Parses a string that should be the mail's subject according with an expected
 * format.
 */
public interface SubjectHandler {

	/**
	 * The result of the {@link SubjectParser.parse(String)} operation.
	 */
	interface ParsedSubject {

		/**
		 * Returns {@code true} if the subject has the expected format.
		 * 
		 * @return {@code true} if the subject has expected format,
		 *         {@code false} otherwise.
		 */
		boolean hasExpectedFormat();

		/**
		 * Returns the e-mail id.
		 * 
		 * @return the e-mail id.
		 */
		Long getEmailId();

		/**
		 * Returns the "real" subject.<br>
		 * <br>
		 * For example: from "{@code [42] foo}" it returns "{@code foo}".
		 * 
		 * @return the "real" subject.
		 */
		String getRealSubject();

	}

	/**
	 * The result of the {@link SubjectParser.compile(Email)} operation.
	 */
	public interface CompiledSubject {

		/**
		 * Returns the compiled subject.
		 * 
		 * @return the compiled subject.
		 */
		String getSubject();

	}

	/**
	 * Parses the specified subject.
	 */
	ParsedSubject parse(String subject);

	/**
	 * Compiles the subject of the specified email.
	 */
	CompiledSubject compile(Email email);

}
