package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.ExtendedAttributes;

public class XpdlActivityExtendedAttributes extends XpdlExtendedAttributes {

	private final XpdlActivity activity;

	XpdlActivityExtendedAttributes(final XpdlActivity activity) {
		super(activity.doc);
		this.activity = activity;
	}

	protected ExtendedAttributes extendedAttributes() {
		return activity.inner.getExtendedAttributes();
	}

}
