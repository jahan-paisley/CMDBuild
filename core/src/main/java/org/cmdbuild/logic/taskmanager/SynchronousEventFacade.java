package org.cmdbuild.logic.taskmanager;

public interface SynchronousEventFacade {

	void create(SynchronousEventTask task);

	void delete(SynchronousEventTask task);

}
