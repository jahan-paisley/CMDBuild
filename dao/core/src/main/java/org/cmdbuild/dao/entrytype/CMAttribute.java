package org.cmdbuild.dao.entrytype;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;

public interface CMAttribute extends Deactivable {

	/**
	 * How the attribute is showed (or not) in the form.
	 */
	enum Mode {
		WRITE, //
		READ, //
		HIDDEN, //
	}

	CMEntryType getOwner();

	CMAttributeType<?> getType();

	String getName();

	String getDescription();

	/**
	 * @return {@code true} if attribute is system, {@code false} otherwise.
	 */
	boolean isSystem();

	/**
	 * Attributes can be inherited.
	 * 
	 * @return {@code true} if attribute is inherited, {@code false} otherwise.
	 */
	boolean isInherited();

	/**
	 * Attributes can be showed in the list/grid.
	 * 
	 * @return {@code true} if attribute can be showed, {@code false} otherwise.
	 */
	boolean isDisplayableInList();

	/**
	 * Attribute's values must be specified or not.
	 * 
	 * @return {@code true} if attribute's value must be specified,
	 *         {@code false} otherwise.
	 */
	boolean isMandatory();

	/**
	 * Attribute's values must be unique or not
	 * 
	 * @return {@code true} if attribute's value must be unique, {@code false}
	 *         otherwise.
	 */
	boolean isUnique();

	/**
	 * Returns how the field is showed (or not) in the form.
	 * 
	 * @return the {@link Mode}.
	 */
	Mode getMode();

	/**
	 * Returns the index of the attribute.
	 * 
	 * @return the index of the attribute.
	 */
	int getIndex();

	/**
	 * Returns the default value of the attribute.
	 * 
	 * @return the default value of the attribute.
	 */
	String getDefaultValue();

	/**
	 * Returns the group of the attribute.
	 * 
	 * @return the group of the attribute.
	 */
	String getGroup();

	/**
	 * Returns the class order of the attribute.
	 * 
	 * @return the class order of the attribute.
	 */
	int getClassOrder();

	/**
	 * Returns the editor that must be used for {@link TextAttributeType}.
	 * 
	 * @return "HTML" if the editor is HTML, something else if not.
	 */
	String getEditorType();

	/**
	 * Returns the filter to apply to {@link ReferenceAttributeType}
	 * 
	 * @return the filter to apply
	 */
	String getFilter();

	String getForeignKeyDestinationClassName();

}
