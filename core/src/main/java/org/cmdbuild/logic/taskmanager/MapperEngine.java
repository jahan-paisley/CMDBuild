package org.cmdbuild.logic.taskmanager;

public interface MapperEngine {

	void accept(MapperEngineVisitor visitor);

}
