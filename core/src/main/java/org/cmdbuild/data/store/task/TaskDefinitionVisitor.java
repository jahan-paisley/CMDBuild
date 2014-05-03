package org.cmdbuild.data.store.task;

public interface TaskDefinitionVisitor {

	void visit(final ReadEmailTaskDefinition taskDefinition);

	void visit(final StartWorkflowTaskDefinition taskDefinition);

	void visit(final SynchronousEventTaskDefinition taskDefinition);

}
