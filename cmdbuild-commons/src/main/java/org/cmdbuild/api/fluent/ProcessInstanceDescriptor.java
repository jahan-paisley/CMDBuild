package org.cmdbuild.api.fluent;

public class ProcessInstanceDescriptor extends CardDescriptor {

	private final String processInstanceId;

	public ProcessInstanceDescriptor(final String className, final Integer id, final String processInstanceId) {
		super(className, id);
		this.processInstanceId = processInstanceId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

}
