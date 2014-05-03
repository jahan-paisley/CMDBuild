package org.cmdbuild.services.scheduler;

import org.cmdbuild.exception.SchedulerException.SchedulerExceptionType;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;

public class DefaultSchedulerExeptionFactory implements SchedulerExeptionFactory {

	@Override
	public RuntimeException internal(final Throwable cause) {
		return SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(cause.getMessage());
	}

	@Override
	public RuntimeException cronExpression(final Throwable cause, final String expression) {
		return SchedulerExceptionType.ILLEGAL_CRON_EXPRESSION.createException(expression);
	}

}
