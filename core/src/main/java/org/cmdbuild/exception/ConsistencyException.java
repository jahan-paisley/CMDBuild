package org.cmdbuild.exception;

public class ConsistencyException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final ConsistencyExceptionType type;

	public enum ConsistencyExceptionType {
		/**
		 * Parameters must be defined in the following order: lockerUsername,
		 * timeSinceLock
		 */
		LOCKED_CARD,

		/**
		 * Thrown when try to update an old version of a processInstance.
		 * This could happen if try to edit an process from an out of date
		 * grid
		 */
		OUT_OF_DATE_PROCESS,

		/**
		 * Thrown when try to delete a card that has
		 * some active relations
		 */
		ORM_CANT_DELETE_CARD_WITH_RELATION;

		public ConsistencyException createException(final String... parameters) {
			return new ConsistencyException(this, parameters);
		}
	}

	private ConsistencyException(final ConsistencyExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public ConsistencyExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
