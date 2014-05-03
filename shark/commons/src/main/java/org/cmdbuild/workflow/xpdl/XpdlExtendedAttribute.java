package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.ExtendedAttribute;

public class XpdlExtendedAttribute {

	private final String key;
	private final String value;

	public XpdlExtendedAttribute(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public static XpdlExtendedAttribute newInstance(ExtendedAttribute xa) {
		if (xa == null) {
			return null;
		} else {
			return new XpdlExtendedAttribute(xa.getName(), xa.getVValue());
		}
	}
}
