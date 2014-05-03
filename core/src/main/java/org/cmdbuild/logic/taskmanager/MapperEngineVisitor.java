package org.cmdbuild.logic.taskmanager;

public interface MapperEngineVisitor {

	void visit(KeyValueMapperEngine mapper);

	void visit(NullMapperEngine mapper);

}
