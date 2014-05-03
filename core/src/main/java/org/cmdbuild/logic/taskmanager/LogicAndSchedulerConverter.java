package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.scheduler.Job;

public interface LogicAndSchedulerConverter {

	interface LogicAsSourceConverter {

		Job toJob();

	}

	LogicAsSourceConverter from(ScheduledTask source);

}