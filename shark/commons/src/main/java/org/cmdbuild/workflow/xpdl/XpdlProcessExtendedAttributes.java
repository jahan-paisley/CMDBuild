package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.ExtendedAttributes;

public class XpdlProcessExtendedAttributes extends XpdlExtendedAttributes {

	private final XpdlProcess proc;

	XpdlProcessExtendedAttributes(final XpdlProcess proc) {
		super(proc.getDocument());
		this.proc = proc;
	}

	protected ExtendedAttributes extendedAttributes() {
		return proc.inner.getExtendedAttributes();
	}

}
