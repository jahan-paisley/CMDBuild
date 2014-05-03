package org.cmdbuild.services.soap.structure;

import java.util.List;

public class WorkflowWidgetDefinition {	
	private String type;
	private String identifier;
	private List<WorkflowWidgetDefinitionParameter> parameters;

	public WorkflowWidgetDefinition() { } 
	
	public WorkflowWidgetDefinition(String type, String identifier) {
		this.type = type;
		this.identifier = identifier;
	}

	public void setParameters(List<WorkflowWidgetDefinitionParameter> parameters) {
		this.parameters = parameters;
	}
	
	public List<WorkflowWidgetDefinitionParameter> getParameters() {
		return parameters;
	}

	public String getType() {
		return type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
