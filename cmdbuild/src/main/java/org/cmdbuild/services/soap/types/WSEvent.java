package org.cmdbuild.services.soap.types;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AbstractEvent")
public abstract class WSEvent {

	public interface Visitor {
		void visit(WSProcessStartEvent event);
		void visit(WSProcessUpdateEvent event);
	}

	public abstract void accept(Visitor visitor);
}
