package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultSchedulerFacade implements SchedulerFacade {

	private static final Logger logger = Logic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(DefaultSchedulerFacade.class.getName());

	private final LogicAndSchedulerConverter converter;
	private final SchedulerService schedulerService;

	public DefaultSchedulerFacade(final SchedulerService schedulerService, final LogicAndSchedulerConverter converter) {
		this.converter = converter;
		this.schedulerService = schedulerService;
	}

	@Override
	public void create(final ScheduledTask task) {
		logger.info(MARKER, "creating a new scheduled task '{}'", task);
		if (task.isActive()) {
			final Job serviceJob = converter.from(task).toJob();
			final Trigger trigger = RecurringTrigger.at(addSecondsField(task.getCronExpression()));
			schedulerService.add(serviceJob, trigger);
		}
	}

	private String addSecondsField(final String cronExpression) {
		return "0 " + cronExpression;
	}

	@Override
	public void delete(final ScheduledTask task) {
		logger.info(MARKER, "deleting an existing scheduled task '{}'", task);
		if (task.isActive()) {
			final Job job = converter.from(task).toJob();
			schedulerService.remove(job);
		}
	}

}
