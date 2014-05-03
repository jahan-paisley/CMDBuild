package org.cmdbuild.bim.model.implementation;

import org.cmdbuild.bim.model.EntityDefinition;

public class ReferenceAttributeDefinition extends DefaultAttributeDefinition {

	private EntityDefinition reference;

	public ReferenceAttributeDefinition(String attributeName) {
		super(attributeName);
	}

	@Override
	public EntityDefinition getReference() {
		return reference;
	}

	public void setReference(EntityDefinition referencedEntity) {
		this.reference = referencedEntity;
	}
	
	public String toString(){
		return "REFERENCE TO --> " + this.getName();
	}

}
