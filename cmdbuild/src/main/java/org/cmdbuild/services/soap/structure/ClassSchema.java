package org.cmdbuild.services.soap.structure;

import java.util.List;

public class ClassSchema {

	private String name;
	private String description;
	private boolean superClass;
	private List<AttributeSchema> attributes;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public boolean isSuperClass() {
		return superClass;
	}

	public void setSuperClass(final boolean superClass) {
		this.superClass = superClass;
	}

	public List<AttributeSchema> getAttributes() {
		return attributes;
	}

	public void setAttributes(final List<AttributeSchema> attributes) {
		this.attributes = attributes;
	}

}
