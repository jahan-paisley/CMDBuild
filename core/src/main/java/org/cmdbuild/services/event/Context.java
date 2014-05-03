package org.cmdbuild.services.event;

public interface Context {

	void accept(ContextVisitor visitor);

}
