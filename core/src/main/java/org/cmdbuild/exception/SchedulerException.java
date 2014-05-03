package org.cmdbuild.exception;

public class SchedulerException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final SchedulerExceptionType type;

	public enum SchedulerExceptionType {
		SCHEDULER_INTERNAL_ERROR, ILLEGAL_CRON_EXPRESSION; // failed expression

		public SchedulerException createException(final String... parameters) {
			return new SchedulerException(this, parameters);
		}
	}

	private SchedulerException(final SchedulerExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public SchedulerExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
