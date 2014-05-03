package org.cmdbuild.scheduler;

public interface SchedulerService {

	void add(Job job, Trigger trigger);

	void remove(Job job);

	void start();

	void stop();

}
