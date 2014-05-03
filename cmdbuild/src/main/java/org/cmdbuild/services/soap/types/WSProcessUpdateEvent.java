package org.cmdbuild.services.soap.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ProcessUpdateEvent")
public class WSProcessUpdateEvent extends WSWorkflowEvent {

	@Override
	public void accept(final Visitor visitor) {
		visitor.visit(this);
	}

}
