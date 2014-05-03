package org.cmdbuild.scheduler;

public interface TriggerVisitor {

	void visit(OneTimeTrigger trigger);

	void visit(RecurringTrigger trigger);

}
