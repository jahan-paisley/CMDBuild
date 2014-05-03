package org.cmdbuild.logic.data;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.workflow.CMProcessClass;
import org.joda.time.DateTime;

public class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static CMClassDefinition definitionForNew(final EntryType entryType, final CMClass parentClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return fromName(entryType);
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public String getDescription() {
				return entryType.getDescription();
			}

			@Override
			public CMClass getParent() {
				return parentClass;
			}

			@Override
			public boolean isSuperClass() {
				return entryType.isSuperClass();
			}

			@Override
			public boolean isHoldingHistory() {
				return entryType.isHoldingHistory();
			}

			@Override
			public boolean isActive() {
				return entryType.isActive();
			}

			@Override
			public boolean isUserStoppable() {
				return entryType.isUserStoppable();
			}

			@Override
			public boolean isSystem() {
				return entryType.isSystem();
			}

		};
	}

	private static CMIdentifier fromName(final EntryType entryType) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return entryType.getName();
			}

			@Override
			public String getNameSpace() {
				return entryType.getNamespace();
			}

		};
	}

	public static CMClassDefinition definitionForExisting(final EntryType clazz, final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existingClass.getIdentifier();
			}

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getDescription() {
				return clazz.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return clazz.isActive();
			}

			@Override
			public boolean isUserStoppable() {
				return clazz.isUserStoppable();
			}

			@Override
			public boolean isSystem() {
				return existingClass.isSystem();
			}

		};
	}

	public static CMClassDefinition unactive(final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existingClass.getIdentifier();
			}

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getDescription() {
				return existingClass.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public boolean isUserStoppable() {
				final boolean userStoppable;
				if (existingClass instanceof CMProcessClass) {
					userStoppable = CMProcessClass.class.cast(existingClass).isUserStoppable();
				} else {
					userStoppable = false;
				}
				return userStoppable;
			}

			@Override
			public boolean isSystem() {
				return existingClass.isSystem();
			}

		};
	}

	public static CMAttributeDefinition definitionForNew(final Attribute attribute, final CMEntryType owner) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return attribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return owner;
			}

			@Override
			public CMAttributeType<?> getType() {
				return attribute.getType();
			}

			@Override
			public String getDescription() {
				return attribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return attribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return attribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return attribute.isActive();
			}

			@Override
			public Mode getMode() {
				return attribute.getMode();
			}

			@Override
			public int getIndex() {
				return attribute.getIndex();
			}

			@Override
			public String getGroup() {
				return attribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return attribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return attribute.getEditorType();
			}

			@Override
			public String getFilter() {
				return attribute.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return attribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMAttributeDefinition definitionForExisting( //
			final Attribute attributeWithNewValues, //
			final CMAttribute existingAttribute //
	) {

		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			// Some info about the attributes are
			// stored in the CMAttributeType, so for
			// String, Lookup and Decimal use the
			// new attribute type to update these info
			public CMAttributeType<?> getType() {
				if (existingAttribute.getType() instanceof LookupAttributeType
						&& attributeWithNewValues.getType() instanceof LookupAttributeType) {

					return attributeWithNewValues.getType();
				} else if (existingAttribute.getType() instanceof StringAttributeType
						&& attributeWithNewValues.getType() instanceof StringAttributeType) {

					return attributeWithNewValues.getType();
				} else if (existingAttribute.getType() instanceof DecimalAttributeType
						&& attributeWithNewValues.getType() instanceof DecimalAttributeType) {

					return attributeWithNewValues.getType();
				} else {
					return existingAttribute.getType();
				}
			}

			@Override
			public String getDescription() {
				return attributeWithNewValues.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return attributeWithNewValues.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attributeWithNewValues.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attributeWithNewValues.isUnique();
			}

			@Override
			public boolean isActive() {
				return attributeWithNewValues.isActive();
			}

			@Override
			public Mode getMode() {
				return attributeWithNewValues.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return attributeWithNewValues.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return attributeWithNewValues.getEditorType();
			}

			@Override
			public String getFilter() {
				return attributeWithNewValues.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return existingAttribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMAttributeDefinition definitionForReordering(final Attribute attribute,
			final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return existingAttribute.isActive();
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return attribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return existingAttribute.getEditorType();
			}

			@Override
			public String getFilter() {
				return existingAttribute.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return existingAttribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMAttributeDefinition definitionForClassOrdering(final Attribute attribute,
			final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return existingAttribute.isActive();
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return attribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return existingAttribute.getEditorType();
			}

			@Override
			public String getFilter() {
				return existingAttribute.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return existingAttribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMAttributeDefinition unactive(final CMAttribute existingAttribute) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return existingAttribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return existingAttribute.getOwner();
			}

			@Override
			public CMAttributeType<?> getType() {
				return existingAttribute.getType();
			}

			@Override
			public String getDescription() {
				return existingAttribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return existingAttribute.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return existingAttribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return existingAttribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return existingAttribute.isUnique();
			}

			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public Mode getMode() {
				return existingAttribute.getMode();
			}

			@Override
			public int getIndex() {
				return existingAttribute.getIndex();
			}

			@Override
			public String getGroup() {
				return existingAttribute.getGroup();
			}

			@Override
			public int getClassOrder() {
				return existingAttribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return existingAttribute.getEditorType();
			}

			@Override
			public String getFilter() {
				return existingAttribute.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return existingAttribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMDomainDefinition definitionForNew(final Domain domain, final CMClass class1, final CMClass class2) {
		return new CMDomainDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return fromName(domain);
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public CMClass getClass1() {
				return class1;
			}

			@Override
			public CMClass getClass2() {
				return class2;
			}

			@Override
			public String getDescription() {
				return domain.getDescription();
			}

			@Override
			public String getDirectDescription() {
				return domain.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domain.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return domain.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return domain.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domain.getMasterDetailDescription();
			}

			@Override
			public boolean isActive() {
				return domain.isActive();
			}

		};
	}

	private static CMIdentifier fromName(final Domain domain) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return domain.getName();
			}

			@Override
			public String getNameSpace() {
				// TODO must be done ASAP
				return null;
			}

		};
	}

	public static CMDomainDefinition definitionForExisting(final Domain domainWithChanges, final CMDomain existing) {
		return new CMDomainDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existing.getIdentifier();
			}

			@Override
			public Long getId() {
				return existing.getId();
			}

			@Override
			public CMClass getClass1() {
				return existing.getClass1();
			}

			@Override
			public CMClass getClass2() {
				return existing.getClass2();
			}

			@Override
			public String getDescription() {
				return domainWithChanges.getDescription();
			}

			@Override
			public String getDirectDescription() {
				return domainWithChanges.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domainWithChanges.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return existing.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return domainWithChanges.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domainWithChanges.getMasterDetailDescription();
			}

			@Override
			public boolean isActive() {
				return domainWithChanges.isActive();
			}

		};
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return an empty String, otherwise cast the object to string
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static String readString(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);
		if (value == null) {
			return "";
		} else {
			return (String) value;
		}
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return an false, otherwise cast the object to boolean
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static boolean readBoolean(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);
		if (value == null) {
			return false;
		} else {
			return (Boolean) value;
		}
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return null, otherwise try to cast the object to Long
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static Long readLong(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);

		if (value == null) {
			return null;
		} else if (value instanceof Long) {
			return (Long) value;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else {
			return null;
		}
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return null, otherwise cast it to ReferenceCard
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static IdAndDescription readCardReference(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);

		if (value != null) {
			return (IdAndDescription) value;
		}

		return null;
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return null, otherwise try to cast the object to org.joda.time.DateTime
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static DateTime readDateTime(final CMCard card, final String attributeName) {
		Object value = card.get(attributeName);

		if (value instanceof DateTime) {
			return (DateTime) value;
		} else if (value instanceof java.sql.Date) {
			return new DateTime(((java.util.Date) value).getTime());
		}

		return null;
	}

}
