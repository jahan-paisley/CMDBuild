package org.cmdbuild.logic.validation;

/**
 * Interface that can be implemented by each class whose purpose is to validate
 * something .
 */
public interface Validator {

	/**
	 * Error for validation operations.
	 */
	class ValidationError extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ValidationError() {
			super();
		}

		public ValidationError(final String message, final Throwable cause) {
			super(message, cause);
		}

		public ValidationError(final String message) {
			super(message);
		}

		public ValidationError(final Throwable cause) {
			super(cause);
		}

	}

	/**
	 * If validation was successful, it returns nothing. If the validation
	 * fails, it throws an {@link ValidationError}.
	 * 
	 * @throws {@link ValidationError} on validation error.
	 */
	void validate() throws ValidationError;

}
