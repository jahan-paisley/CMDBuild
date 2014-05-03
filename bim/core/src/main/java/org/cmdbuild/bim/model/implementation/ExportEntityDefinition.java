package org.cmdbuild.bim.model.implementation;

import java.util.List;

import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.EntityDefinition;

public class ExportEntityDefinition implements EntityDefinition {

	private String typeName, label, shape, containerAttribute;

	public ExportEntityDefinition(String name) {
		this.typeName = name;
		label = "";
		shape = "";
		containerAttribute = "";
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getShape() {
		return shape;
	}
	
	@Override
	public String getContainerAttribute() {
		return containerAttribute;
	}

	@Override
	public void setShape(String shape) {
		this.shape = shape;
	}

	@Override
	public void setContainerAttribute(String containerAttribute) {
		this.containerAttribute = containerAttribute;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public List<AttributeDefinition> getAttributes() {
		return null;
	}


}
