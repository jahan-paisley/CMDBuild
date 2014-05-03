package support.scheduler.quartz;

import org.cmdbuild.scheduler.SchedulerService;

public class SelfRemovingJob extends ExecutionListenerJob {

	private final SchedulerService scheduler;

	public SelfRemovingJob(final SchedulerService scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void execute() {
		scheduler.remove(this);
		super.execute();
	}

}
