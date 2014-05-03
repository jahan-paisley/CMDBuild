package org.cmdbuild.services.soap.structure;

import java.util.List;


public class ActivitySchema {
	
	private List<WorkflowWidgetDefinition> widgets;
	private List<AttributeSchema> attributes;
	
	public ActivitySchema(){}

	public List<WorkflowWidgetDefinition> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<WorkflowWidgetDefinition> widgets) {
		this.widgets = widgets;
	}

	public List<AttributeSchema> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeSchema> attributes) {
		this.attributes = attributes;
	}
	
}
