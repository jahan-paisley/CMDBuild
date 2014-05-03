package org.cmdbuild.scheduler;

public interface Trigger {

	void accept(TriggerVisitor visitor);

}
