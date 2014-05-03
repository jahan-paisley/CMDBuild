package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;

abstract class XpdlExtendedAttributes {

	private final XpdlDocument doc;

	XpdlExtendedAttributes(final XpdlDocument doc) {
		this.doc = doc;
	}

	public void addExtendedAttribute(final String key, final String value) {
		doc.turnReadWrite();
		final ExtendedAttributes xattrs = extendedAttributes();
		final ExtendedAttribute xa = (ExtendedAttribute) xattrs.generateNewElement();
		xa.setName(key);
		xa.setVValue(value);
		xattrs.add(xa);
	}

	public void addOrModifyExtendedAttribute(final String key, final String value) {
		final ExtendedAttribute xa = getFirstExtendedAttribute(key);
		if (xa != null) {
			xa.setVValue(value);
		} else {
			addExtendedAttribute(key, value);
		}
	}

	public String getFirstExtendedAttributeValue(final String key) {
		final ExtendedAttribute xa = getFirstExtendedAttribute(key);
		if (xa == null) {
			return null;
		}
		return xa.getVValue();
	}

	protected ExtendedAttribute getFirstExtendedAttribute(final String key) {
		final ExtendedAttributes xattrs = extendedAttributes();
		if (xattrs == null) {
			return null;
		}
		return xattrs.getFirstExtendedAttributeForName(key);
	}

	protected abstract ExtendedAttributes extendedAttributes();
}
