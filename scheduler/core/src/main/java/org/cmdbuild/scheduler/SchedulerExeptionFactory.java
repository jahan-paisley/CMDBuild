package org.cmdbuild.scheduler;

public interface SchedulerExeptionFactory {

	RuntimeException internal(Throwable cause);

	RuntimeException cronExpression(Throwable cause, String expression);

}
