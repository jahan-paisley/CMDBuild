package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.Logic;

/**
 * Handles all Task Manager operations.
 */
public interface TaskManagerLogic extends Logic {

	/**
	 * Creates a new {@link Task}.
	 */
	Long create(Task task);

	/**
	 * Reads all {@link Task}s.
	 */
	Iterable<Task> read();

	/**
	 * Reads all {@link Task}s for specified type.
	 */
	Iterable<Task> read(Class<? extends Task> type);

	/**
	 * Reads {@link Task}'s details.
	 */
	<T extends Task> T read(T task, Class<T> type);

	/**
	 * Updates an existing {@link Task}.
	 */
	void update(Task task);

	/**
	 * Deletes an existing {@link Task}.
	 */
	void delete(Task task);

	/**
	 * Activates the {@link Task} with the specified id.
	 */
	void activate(Long id);

	/**
	 * Deactivates the {@link Task} with the specified id.
	 */
	void deactivate(Long id);

}
