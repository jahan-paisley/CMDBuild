package org.cmdbuild.bim.model.implementation;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.EntityDefinition;

public class ImportEntityDefinition implements EntityDefinition {

	private String typeName;
	private String label;
	List<AttributeDefinition> attributes;

	public ImportEntityDefinition(String name) {
		attributes = new ArrayList<AttributeDefinition>();
		label = "";
		this.typeName = name;
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
	public List<AttributeDefinition> getAttributes() {
		return attributes;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getShape() {
		return null;
	}

	@Override
	public void setShape(String shape) {
	}

	@Override
	public void setContainerAttribute(String containerAttribute) {
	}

	@Override
	public String getContainerAttribute() {
		return null;
	}

}
