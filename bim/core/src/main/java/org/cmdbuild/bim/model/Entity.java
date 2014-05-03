package org.cmdbuild.bim.model;

import java.util.Map;

import org.cmdbuild.bim.utils.BimConstants;

public interface Entity {

	public static final String KEYATTRIBUTE = BimConstants.IFC_GLOBALID;

	final Entity NULL_ENTITY = new Entity() {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public Map<String, Attribute> getAttributes() {
			return null;
		}

		@Override
		public Attribute getAttributeByName(final String attributeName) {
			return null;
		}

		@Override
		public String getKey() {
			return "";
		}

		@Override
		public String getTypeName() {
			return "";
		}
		
		@Override
		public String toString() {
			return "NULL_ENTITY";
		}

	};

	boolean isValid();
	
	Map<String, Attribute> getAttributes();
	
	Attribute getAttributeByName(String attributeName);

	String getKey();
	
	String getTypeName();

}
