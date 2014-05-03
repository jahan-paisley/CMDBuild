package org.cmdbuild.services.event;

import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;

public interface ContextVisitor {

	void visit(AfterCreate context);

	void visit(BeforeUpdate context);

	void visit(AfterUpdate context);

	void visit(BeforeDelete context);

}
