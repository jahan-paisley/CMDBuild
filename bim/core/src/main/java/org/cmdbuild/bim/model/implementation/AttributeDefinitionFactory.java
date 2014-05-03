package org.cmdbuild.bim.model.implementation;

import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.service.BimError;

public class AttributeDefinitionFactory {
	
	private String type; 

	public AttributeDefinitionFactory(String type) {
		this.type = type;
	}

	public AttributeDefinition createAttribute(String attributeName) {
		if(type.equals("simple")){
			return new SimpleAttributeDefinition(attributeName);
		}else if(type.equals("reference")){
			return new ReferenceAttributeDefinition(attributeName);
		}else if(type.equals("multiple")){
			return new ListAttributeDefinition(attributeName);
		}else{
			throw new BimError("Error in create attribute");
		}
	}

}
