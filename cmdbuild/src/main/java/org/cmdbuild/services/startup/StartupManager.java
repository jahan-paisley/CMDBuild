package org.cmdbuild.services.startup;

/**
 * Handles the startup of multiple {@link Startable} objects according to some
 * {@link Condition}s.
 * 
 * @since 2.2
 */
public interface StartupManager {

	interface Startable {

		void start();

	}

	interface Condition {

		/**
		 * @return {@code true} if the condition is satisfied, {@code false}
		 *         otherwise.
		 */
		boolean satisfied();

	}

	/**
	 * Adds a {@link Startable} object with a specific {@link Condition}.
	 * 
	 * @param startable
	 * @param condition
	 */
	void add(Startable startable, Condition condition);

	/**
	 * Starts all {@link Startable} objects whose specific {@link Condition} is
	 * satisfied and it makes sure that those objects are not started again.
	 */
	void start();

}
