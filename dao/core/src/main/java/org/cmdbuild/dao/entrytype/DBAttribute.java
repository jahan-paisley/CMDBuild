package org.cmdbuild.dao.entrytype;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class DBAttribute implements CMAttribute {

	public static class AttributeMetadata extends EntryTypeMetadata implements CMAttributeType.Meta {

		public static final String REFERENCE_NS = "reference.";

		public static final String BASEDSP = BASE_NS + "basedsp";
		public static final String CLASSORDER = BASE_NS + "classorder";
		public static final String DEFAULT = BASE_NS + "default";
		public static final String EDITOR_TYPE = BASE_NS + "editor.type";
		public static final String FILTER = BASE_NS + "filter";
		public static final String FIELD_MODE = BASE_NS + "fieldmode";
		public static final String GROUP = BASE_NS + "group";
		public static final String INDEX = BASE_NS + "index";
		public static final String INHERITED = BASE_NS + "inherited";
		public static final String LOOKUP_TYPE = BASE_NS + "lookuptype";
		public static final String MANDATORY = BASE_NS + "mandatory";
		public static final String REFERENCE_DIRECT = BASE_NS + REFERENCE_NS + "direct";
		public static final String REFERENCE_DOMAIN = BASE_NS + REFERENCE_NS + "domain";
		public static final String REFERENCE_TYPE = BASE_NS + REFERENCE_NS + "type";
		public static final String UNIQUE = BASE_NS + "unique";
		public static final String FK_TARGET_CLASS = BASE_NS + "fk.target.class";

		private static final String NOT_LOOKUP_TYPE = null;
		private static final String NOT_REFERENCE_TYPE = null;

		@Override
		public final boolean isLookup() {
			return getLookupType() != NOT_LOOKUP_TYPE;
		}

		@Override
		public final String getLookupType() {
			return defaultIfBlank(get(LOOKUP_TYPE), NOT_LOOKUP_TYPE);
		}

		@Override
		public boolean isReference() {
			return getDomain() != NOT_REFERENCE_TYPE;
		}

		@Override
		public String getDomain() {
			return defaultIfBlank(get(REFERENCE_DOMAIN), NOT_REFERENCE_TYPE);
		}

		public boolean isDisplayableInList() {
			return Boolean.parseBoolean(get(BASEDSP));
		}

		public boolean isMandatory() {
			return Boolean.parseBoolean(get(MANDATORY));
		}

		public boolean isUnique() {
			return Boolean.parseBoolean(get(UNIQUE));
		}

		@Override
		public String getForeignKeyDestinationClassName() {
			return get(FK_TARGET_CLASS);
		}

		@Override
		public boolean isForeignKey() {
			return getForeignKeyDestinationClassName() != null;
		}

		public Mode getMode() {
			// TODO do it better... and remember that tests are our friends!
			final Mode fieldMode;
			final String mode = get(FIELD_MODE);
			if ("hidden".equals(mode)) {
				fieldMode = Mode.HIDDEN;
			} else if ("read".equals(mode)) {
				fieldMode = Mode.READ;
			} else {
				fieldMode = Mode.WRITE;
			}
			return fieldMode;
		}

		public boolean isInherited() {
			return Boolean.parseBoolean(get(INHERITED));
		}

		public int getIndex() {
			return Integer.parseInt(defaultIfBlank(get(INDEX), "-1"));
		}

		public String getDefaultValue() {
			return get(DEFAULT);
		}

		public String getGroup() {
			return get(GROUP);
		}

		public int getClassOrder() {
			return Integer.parseInt(defaultIfBlank(get(CLASSORDER), "0"));
		}

		public String getEditorType() {
			return get(EDITOR_TYPE);
		}

		public String getFilter() {
			return get(FILTER);
		}
	}

	DBEntryType owner; // Set by the entry type when attached
	private final CMAttributeType<?> type;

	// TODO Make name and meta inherited by both DBAttribute and DBEntryType
	private final String name;
	private final AttributeMetadata meta;

	public DBAttribute(final String name, final CMAttributeType<?> type, final AttributeMetadata meta) {
		Validate.notEmpty(name);
		this.owner = null; // TODO Use a null object?
		this.name = name;
		this.type = type;
		this.meta = meta;
	}

	@Override
	public DBEntryType getOwner() {
		return owner;
	}

	@Override
	public CMAttributeType<?> getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return meta.getDescription();
	}

	@Override
	public boolean isSystem() {
		return meta.isSystem();
	}

	@Override
	public boolean isInherited() {
		return meta.isInherited();
	}

	@Override
	public boolean isActive() {
		return meta.isActive();
	}

	@Override
	public boolean isDisplayableInList() {
		return meta.isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return meta.isMandatory();
	}

	@Override
	public boolean isUnique() {
		return meta.isUnique();
	}

	@Override
	public Mode getMode() {
		return meta.getMode();
	}

	@Override
	public int getIndex() {
		return meta.getIndex();
	}

	@Override
	public String getDefaultValue() {
		return meta.getDefaultValue();
	}

	@Override
	public String getGroup() {
		return meta.getGroup();
	}

	@Override
	public int getClassOrder() {
		return meta.getClassOrder();
	}

	@Override
	public String getEditorType() {
		return meta.getEditorType();
	}

	@Override
	public String getFilter() {
		return meta.getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return meta.getForeignKeyDestinationClassName();
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return String.format("%s.%s", owner, name);
	}

}
