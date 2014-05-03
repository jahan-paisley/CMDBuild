package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.Application;
import org.enhydra.jxpdl.elements.ExtendedAttributes;

public class XpdlApplication implements XpdlExtendedAttributesHolder {

	private final XpdlDocument doc;
	private final Application inner;

	private final XpdlExtendedAttributes extendedAttributes;

	XpdlApplication(final XpdlDocument document, final Application application) {
		this.doc = document;
		this.inner = application;
		this.extendedAttributes = new XpdlApplicationExtendedAttributes(this);
	}

	XpdlDocument getXpdlDocument() {
		return doc;
	}

	ExtendedAttributes getInnerExtendedAttributes() {
		return inner.getExtendedAttributes();
	}

	public String getId() {
		return inner.getId();
	}

	@Override
	public void addExtendedAttribute(final String key, final String value) {
		extendedAttributes.addExtendedAttribute(key, value);
	}

	@Override
	public String getFirstExtendedAttributeValue(final String key) {
		return extendedAttributes.getFirstExtendedAttributeValue(key);
	}

}
