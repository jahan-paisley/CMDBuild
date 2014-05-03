package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.services.event.Observer;

public interface LogicAndObserverConverter {

	interface LogicAsSourceConverter {

		Observer toObserver();

	}

	LogicAsSourceConverter from(SynchronousEventTask source);

}