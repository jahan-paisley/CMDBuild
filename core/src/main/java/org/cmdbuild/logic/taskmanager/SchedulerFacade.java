package org.cmdbuild.logic.taskmanager;

public interface SchedulerFacade {

	/**
	 * Creates a new {@link ScheduledTask}.
	 */
	void create(ScheduledTask task);

	/**
	 * Deletes an existing {@link ScheduledTask}.
	 */
	void delete(ScheduledTask task);

}
