package org.cmdbuild.data.store.task;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Groupable;

public class TaskParameterGroupable implements Groupable {

	public static TaskParameterGroupable groupedBy(final TaskDefinition owner) {
		return new TaskParameterGroupable(owner);
	}

	private final TaskDefinition owner;

	private TaskParameterGroupable(final TaskDefinition schedulerJob) {
		Validate.notNull(schedulerJob, "owner cannot be null");
		this.owner = schedulerJob;
	}

	@Override
	public String getGroupAttributeName() {
		return TaskParameterConverter.OWNER;
	}

	@Override
	public Object getGroupAttributeValue() {
		return owner.getId();
	}

}