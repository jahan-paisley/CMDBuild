package org.cmdbuild.workflow.xpdl;

import org.apache.commons.lang3.Validate;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.Transition;

public class XpdlTransition {

	private final XpdlProcess process;
	private final Transition inner;

	XpdlTransition(final XpdlProcess process, final Transition transition) {
		Validate.notNull(process);
		Validate.notNull(transition);
		this.process = process;
		inner = transition;
	}

	public XpdlActivity getDestination() {
		return new XpdlActivity(process, inner.getToActivity());
	}

	public XpdlActivity getSource() {
		return new XpdlActivity(process, inner.getFromActivity());
	}

	public boolean hasCondition() {
		return inner.getCondition().getType() != XPDLConstants.CONDITION_TYPE_NONE;
	}
}
