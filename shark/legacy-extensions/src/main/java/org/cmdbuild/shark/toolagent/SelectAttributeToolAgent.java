package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public class SelectAttributeToolAgent extends ManageAttributeToolAgent {

	private static final String ATTRIBUTE_VALUE = "AttributeValue";

	@Override
	protected String outputName() {
		return ATTRIBUTE_VALUE;
	}
	
	@Override
	protected Object attributeValue() {
		final Object value = super.attributeValue();
		if (value.getClass().equals(LookupType.class)) {
			return ((Integer) ((LookupType) value).getId()).toString();
		} else if (value.getClass().equals(ReferenceType.class)) {
			return ((Integer) ((ReferenceType) value).getId()).toString();
		} else {
			return value.toString();
		}
	}

}
