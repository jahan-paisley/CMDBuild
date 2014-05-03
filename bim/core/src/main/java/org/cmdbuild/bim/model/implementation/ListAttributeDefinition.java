package org.cmdbuild.bim.model.implementation;

import java.util.List;

import org.cmdbuild.bim.model.EntityDefinition;

import com.google.common.collect.Lists;

public class ListAttributeDefinition extends DefaultAttributeDefinition {

	private EntityDefinition reference = EntityDefinition.NULL_ENTITYDEFINITION;
	private List<EntityDefinition> allReferences = Lists.newArrayList();

	public List<EntityDefinition> getAllReferences() {
		return allReferences;
	}

	public ListAttributeDefinition(String attributeName) {
		super(attributeName);
	}

	public void setReference(EntityDefinition referencedEntity) {
		this.reference = referencedEntity;
	}

	public EntityDefinition getReference() {
		return reference;
	}
	
	public String toString(){
		return "LIST OF --> " + this.getName();
	}

}
