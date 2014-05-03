package org.cmdbuild.scheduler;

public interface Job {

	/**
	 * 
	 * @return the name used to identify the Job
	 */
	String getName();

	/**
	 * The method called by the scheduler
	 */
	void execute();

}
