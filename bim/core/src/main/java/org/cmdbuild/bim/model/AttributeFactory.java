package org.cmdbuild.bim.model;

public interface AttributeFactory {

	final AttributeFactory NULL_ATTRIBUTE_FACTORY = new AttributeFactory() {

		@Override
		public Attribute create() {
			return Attribute.NULL_ATTRIBUTE;
		}

	};

	public Attribute create();
}
