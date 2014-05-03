package org.cmdbuild.data.store.task;

import org.cmdbuild.data.store.Store;

public interface TaskStore extends Store<Task> {

	Task read(Long id);

}