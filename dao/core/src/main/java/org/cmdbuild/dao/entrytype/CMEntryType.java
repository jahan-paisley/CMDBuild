package org.cmdbuild.dao.entrytype;

import org.cmdbuild.auth.acl.CMPrivilegedObject;

public interface CMEntryType extends Deactivable, CMPrivilegedObject {

	interface CMEntryTypeDefinition {

		/**
		 * Returns the entry type identifier.
		 * 
		 * @return the entry type identifier.
		 */
		CMIdentifier getIdentifier();

		/**
		 * Returns the entry type id.
		 * 
		 * @return the entry type id, {@code null} if missing.
		 */
		Long getId();

	}

	void accept(CMEntryTypeVisitor visitor);

	Long getId();

	String getName();

	CMIdentifier getIdentifier();

	String getDescription();

	boolean isSystem();

	boolean isSystemButUsable();

	boolean isBaseClass();

	/**
	 * Indicates if it holds historic data
	 * 
	 * @return if it holds historic data
	 */
	boolean holdsHistory();

	/**
	 * Returns active attributes for this entry type.
	 * 
	 * @return attributes in the correct display order
	 */
	Iterable<? extends CMAttribute> getActiveAttributes();

	/**
	 * Returns active and non-active attributes for this entry type.
	 * 
	 * @return attributes in the correct display order
	 */
	Iterable<? extends CMAttribute> getAttributes();

	/**
	 * Returns all attributes for this entry type.
	 * 
	 * @return attributes in the correct display order
	 */
	Iterable<? extends CMAttribute> getAllAttributes();

	CMAttribute getAttribute(String name);

	String getKeyAttributeName();

}
