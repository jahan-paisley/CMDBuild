package org.cmdbuild.workflow.event;

import org.apache.commons.lang3.Validate;

public class WorkflowEvent {

	public enum Type {
		START, UPDATE
	}

	private final Type type;
	private final String processDefinitionId;
	private final String processInstanceId;

	private WorkflowEvent(final Type type, final String processDefinitionId, final String processInstanceId) {
		Validate.notNull(type);
		Validate.notEmpty(processDefinitionId);
		Validate.notEmpty(processInstanceId);
		this.type = type;
		this.processDefinitionId = processDefinitionId;
		this.processInstanceId = processInstanceId;
	}

	public Type getType() {
		return type;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public static WorkflowEvent newProcessStartEvent(final String processDefinitionId, final String processInstanceId) {
		return new WorkflowEvent(Type.START, processDefinitionId, processInstanceId);
	}

	public static WorkflowEvent newProcessUpdateEvent(final String processDefinitionId, final String processInstanceId) {
		return new WorkflowEvent(Type.UPDATE, processDefinitionId, processInstanceId);
	}

}
