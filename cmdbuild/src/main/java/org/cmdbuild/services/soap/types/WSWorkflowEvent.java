package org.cmdbuild.services.soap.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AbstractWorkflowEvent")
public abstract class WSWorkflowEvent extends WSEvent {

	private int sessionId;
	private String processDefinitionId;
	private String processInstanceId;

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(final String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(final String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

}
