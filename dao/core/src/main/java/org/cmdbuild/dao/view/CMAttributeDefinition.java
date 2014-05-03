package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

/**
 * Attribute definition used for creating or updating attributes.
 */
public interface CMAttributeDefinition {

	String getName();

	CMEntryType getOwner();

	CMAttributeType<?> getType();

	String getDescription();

	String getDefaultValue();

	boolean isDisplayableInList();

	boolean isMandatory();

	boolean isUnique();

	boolean isActive();

	Mode getMode();

	int getIndex();

	String getGroup();

	int getClassOrder();

	String getEditorType();

	String getFilter();

	String getForeignKeyDestinationClassName();

}
