package org.cmdbuild.logic.taskmanager;

public interface Task {

	void accept(TaskVistor visitor);

	Long getId();

	String getDescription();

	boolean isActive();

}
