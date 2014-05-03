package org.cmdbuild.data.store;

import org.cmdbuild.data.store.DataViewStore.StorableConverter;

public interface Groupable {

	/**
	 * Returns the name of the attribute that represents the group of the
	 * {@link Storable} objects, {@code null} if there is no grouping. Within a
	 * group the identifier must be unique. Implies a restriction over the
	 * {@link Store#read(Storable)}, {@link Store#update(Storable)} and
	 * {@link Store#list()} methods.
	 * 
	 * @return the name of the attribute or {@code null} if grouping is not
	 *         available.
	 */
	String getGroupAttributeName();

	/**
	 * Returns the name of the group. See
	 * {@link StorableConverter#getGroupAttributeName()}.
	 * 
	 * @return the name of the group.
	 */
	Object getGroupAttributeValue();

}