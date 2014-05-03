package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.elements.Applications;

public class XpdlProcessApplications extends XpdlApplications {

	private final XpdlProcess xpdlProcess;

	XpdlProcessApplications(final XpdlProcess process) {
		super(process.getDocument());
		this.xpdlProcess = process;
	}

	@Override
	protected Applications applications() {
		return xpdlProcess.inner.getApplications();
	}

}
