package org.cmdbuild.dao;

public interface TypeObjectCache {

	/**
	 * Adds an object to the cache.
	 * 
	 * @param typeObject
	 *            the object to be added.
	 */
	void add(CMTypeObject typeObject);

	/**
	 * Removes an object from the cache.
	 * 
	 * @param typeObject
	 *            the object to be removed.
	 */
	void remove(CMTypeObject typeObject);

	/**
	 * Clears the whole cache of the driver.
	 */
	void clear();

	/**
	 * Gets the cache status by type.
	 * 
	 * @param typeObjectClass
	 *            the required type.
	 * 
	 * @return {@code true} if the cache for the required type is empty,
	 *         {@code false} otherwise.
	 */
	boolean isEmpty(Class<? extends CMTypeObject> typeObjectClass);

	/**
	 * Gets all elements by type.
	 * 
	 * @param typeObjectClass
	 *            the required type.
	 * 
	 * @return all cached elements.
	 */
	<T extends CMTypeObject> Iterable<T> fetch(Class<T> typeObjectClass);

}
