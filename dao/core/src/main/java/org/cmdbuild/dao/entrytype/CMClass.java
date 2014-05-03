package org.cmdbuild.dao.entrytype;

import org.cmdbuild.auth.acl.SerializablePrivilege;

public interface CMClass extends CMEntryType, SerializablePrivilege {

	interface CMClassDefinition extends CMEntryTypeDefinition {

		String getDescription();

		/**
		 * Returns the class's parent.
		 * 
		 * @return the class's parent, {@code null} if the class have no parent.
		 */
		CMClass getParent();

		/**
		 * Tells if the class must be a superclass.
		 * 
		 * @return {@code true} if the class must be a superclass, {@code false}
		 *         otherwise.
		 */
		boolean isSuperClass();

		/**
		 * Tells if the class must hold history.
		 * 
		 * @return {@code true} if the class holds history, {@code false}
		 *         otherwise.
		 */
		boolean isHoldingHistory();

		/**
		 * Tells if the class must be active or not.
		 * 
		 * @return {@code true} if the class must be a active, {@code false}
		 *         otherwise.
		 */
		boolean isActive();

		/**
		 * Tells if the process class is stoppable by a user.
		 * 
		 * @return {@code true} if the process class is stoppable by user,
		 *         {@code false} otherwise.
		 */
		boolean isUserStoppable();

		/**
		 * 
		 * @return true if the class is a system class (mode reserved), false
		 *         otherwise
		 */
		boolean isSystem();

	}

	CMClass getParent();

	Iterable<? extends CMClass> getChildren();

	Iterable<? extends CMClass> getLeaves();

	Iterable<? extends CMClass> getDescendants();

	boolean isAncestorOf(CMClass cmClass);

	boolean isSuperclass();

	boolean isSimple();

	/**
	 * The code attribute is supposed to be unique for the class and it should
	 * be human-readable. It is used to identify cards in the CSV import and
	 * wherever a numeric key is not practical.
	 * 
	 * @return the name of the attribute containing the card code
	 */
	String getCodeAttributeName();

	/**
	 * A human-readable description of the card.
	 * 
	 * @return the name of the attribute containing the card description
	 */
	String getDescriptionAttributeName();

	boolean isUserStoppable();
}
