package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.Logic;

public interface SchedulerLogic extends Logic {

	void startScheduler();

	void stopScheduler();

}
