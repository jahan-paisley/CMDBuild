package org.cmdbuild.services.soap.structure;

public class WorkflowWidgetSubmission {
	
	private String identifier;
	private WorkflowWidgetSubmissionParameter[] parameters;
	
	public WorkflowWidgetSubmission() { }

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public WorkflowWidgetSubmissionParameter[] getParameters() {
		return parameters;
	}

	public void setParameters(WorkflowWidgetSubmissionParameter[] parameters) {
		this.parameters = parameters;
	}
}
