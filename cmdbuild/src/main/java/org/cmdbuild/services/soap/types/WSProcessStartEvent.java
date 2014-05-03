package org.cmdbuild.services.soap.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ProcessStartEvent")
public class WSProcessStartEvent extends WSWorkflowEvent {

	@Override
	public void accept(final Visitor visitor) {
		visitor.visit(this);
	}

}
