package org.cmdbuild.cql.sqlbuilder.attribute;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class CMFakeAttribute implements CMAttribute {

	private final String name;
	private final String description;
	private final boolean mandatory;
	private final CMEntryType entryType;
	private final CMAttributeType<?> attributeType;

	
	public CMFakeAttribute( //
			final String name, //
			final CMEntryType entryType, //
			final CMAttributeType<?> attributeType, //
			final boolean mandatory
	) {
		this(name, name, entryType, attributeType, mandatory);
	}

	public CMFakeAttribute( //
			final String name, //
			final String description, //
			final CMEntryType entryType, //
			final CMAttributeType<?> attributeType, //
			final boolean mandatory
	) {
		this.name = name;
		this.description = description;
		this.entryType = entryType;
		this.attributeType = attributeType;
		this.mandatory = mandatory;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public CMEntryType getOwner() {
		return entryType;
	}

	@Override
	public CMAttributeType<?> getType() {
		return attributeType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public boolean isInherited() {
		return false;
	}

	@Override
	public boolean isDisplayableInList() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return mandatory;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public Mode getMode() {
		return Mode.WRITE;
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public int getClassOrder() {
		return 0;
	}

	@Override
	public String getEditorType() {
		return null;
	}

	@Override
	public String getFilter() {
		return "";
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return null;
	}

}
