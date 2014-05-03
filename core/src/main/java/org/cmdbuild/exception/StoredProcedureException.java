package org.cmdbuild.exception;

public class StoredProcedureException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final StoredProcedureExceptionType type;

	public enum StoredProcedureExceptionType {
		STOREDPROCEDURE_CANNOT_EXECUTE;

		public StoredProcedureException createException(final String... parameters) {
			return new StoredProcedureException(this, parameters);
		}
	}

	public StoredProcedureException(final StoredProcedureExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public StoredProcedureExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
