package org.cmdbuild.data.store.task;

public interface TaskVisitor {

	void visit(ReadEmailTask task);

	void visit(StartWorkflowTask task);

	void visit(SynchronousEventTask task);

}
