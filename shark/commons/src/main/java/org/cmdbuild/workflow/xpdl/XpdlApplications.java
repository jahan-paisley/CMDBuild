package org.cmdbuild.workflow.xpdl;

import org.enhydra.jxpdl.XMLElement;
import org.enhydra.jxpdl.elements.Application;
import org.enhydra.jxpdl.elements.Applications;

public abstract class XpdlApplications {

	private final XpdlDocument document;

	public XpdlApplications(final XpdlDocument document) {
		this.document = document;
	}

	public XpdlApplication createApplication(final String id) {
		document.turnReadWrite();
		final XMLElement element = applications().generateNewElement();
		final Application application = Application.class.cast(element);
		application.setId(id);
		applications().add(application);
		return new XpdlApplication(document, application);
	}

	protected abstract Applications applications();

}
