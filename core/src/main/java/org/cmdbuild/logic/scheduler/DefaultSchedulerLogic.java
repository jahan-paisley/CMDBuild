package org.cmdbuild.logic.scheduler;

import org.cmdbuild.scheduler.SchedulerService;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final SchedulerService schedulerService;

	public DefaultSchedulerLogic(final SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	@Override
	public void startScheduler() {
		logger.info("starting scheduler service");
		schedulerService.start();
	}

	@Override
	public void stopScheduler() {
		logger.info("stopping scheduler service");
		schedulerService.stop();
	}

}
