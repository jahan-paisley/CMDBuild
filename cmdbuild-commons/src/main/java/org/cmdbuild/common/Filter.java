package org.cmdbuild.common;

/**
 * A filter for generic elements.
 */
public interface Filter<T> {

	/**
	 * Tests whether or not the specified element should be accepted.
	 * 
	 * @param element
	 *            is the generic element.
	 * 
	 * @return {@code true} if and only if {@code element} should accepted.
	 */
	boolean accept(T element);

}
