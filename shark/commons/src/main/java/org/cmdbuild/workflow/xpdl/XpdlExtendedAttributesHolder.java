package org.cmdbuild.workflow.xpdl;


public interface XpdlExtendedAttributesHolder {

	void addExtendedAttribute(String key, String value);
	String getFirstExtendedAttributeValue(String key);
}