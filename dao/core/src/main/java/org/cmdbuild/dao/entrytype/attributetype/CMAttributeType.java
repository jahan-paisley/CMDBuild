package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.function.CMFunction;

/**
 * Visitable type object to be used across the entire DAO layer. The primary use
 * is in {@link CMClass}, but {@link CMFunction} uses it as well and the reports
 * should use it as soon as they are refactored.
 * 
 * TODO: Rename it to CMValueType. NdPaolo: I haven't done it yet because it
 * might affect the DAO feature branch.
 * 
 * @param <T>
 *            Native value object type
 */
public interface CMAttributeType<T> {

	/**
	 * Valid metadata for a value type
	 */
	interface Meta {

		/**
		 * Returns if metadatas define it as a lookup attribute.
		 * 
		 * @return {@code true} if it is a lookup, {@code false} otherwise.
		 */
		boolean isLookup();

		/**
		 * Returns the name of the lookup type for this attribute type. If it is
		 * not a lookup attribute, it can return either null or an empty string
		 * (yes, it is quite horrible).
		 * 
		 * @return the name of the lookup type.
		 */
		String getLookupType();

		/**
		 * Returns if metadatas define it as a reference attribute.
		 * 
		 * @return {@code true} if it is a reference, {@code false} otherwise.
		 */
		boolean isReference();

		/**
		 * Returns the name of the domain for this attribute type. If it is not
		 * a lookup attribute, it can return either null or an empty string.
		 * 
		 * @return the name of the domain.
		 */
		String getDomain();

		/**
		 * Returns true if this attribute type represents a foreign key, false
		 * otherwise
		 * 
		 * @return
		 */
		boolean isForeignKey();

		String getForeignKeyDestinationClassName();

	}

	/**
	 * Casts a value that can be null to the native type.
	 * 
	 * @param value
	 *            of any type.
	 * @return value of the native type.
	 */
	T convertValue(Object value);

	void accept(CMAttributeTypeVisitor visitor);

}
