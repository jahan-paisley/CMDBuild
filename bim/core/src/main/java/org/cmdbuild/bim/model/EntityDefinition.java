package org.cmdbuild.bim.model;

import java.util.List;

public interface EntityDefinition {

	String getTypeName();

	List<AttributeDefinition> getAttributes();

	boolean isValid();

	String getLabel();

	void setLabel(String label);

	String getShape();

	void setShape(String shape);
	
	String getContainerAttribute();
	
	void setContainerAttribute(String containerAttribute);

	final EntityDefinition NULL_ENTITYDEFINITION = new EntityDefinition() {

		@Override
		public String getTypeName() {
			return "";
		}

		@Override
		public String getLabel() {
			return "";
		}

		@Override
		public List<AttributeDefinition> getAttributes() {
			return null;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String getShape() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setLabel(final String label) {
		}

		@Override
		public void setShape(final String shape) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setContainerAttribute(String containerAttribute) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getContainerAttribute() {
			// TODO Auto-generated method stub
			return null;
		}

	};



}
