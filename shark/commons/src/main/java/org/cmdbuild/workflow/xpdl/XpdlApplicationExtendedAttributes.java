package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.ExtendedAttributes;

public class XpdlApplicationExtendedAttributes extends XpdlExtendedAttributes {

	private final XpdlApplication application;

	XpdlApplicationExtendedAttributes(final XpdlApplication application) {
		super(application.getXpdlDocument());
		this.application = application;
	}

	@Override
	protected ExtendedAttributes extendedAttributes() {
		return application.getInnerExtendedAttributes();
	}

}
