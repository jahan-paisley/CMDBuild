package org.cmdbuild.logic.taskmanager;

public interface ScheduledTask extends Task {

	String getCronExpression();

}
