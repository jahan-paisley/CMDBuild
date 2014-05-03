package org.cmdbuild.model.data;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction;

import com.google.common.collect.Maps;

public class Attribute {

	public static enum AttributeTypeBuilder {

		BOOLEAN {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new BooleanAttributeType();
			}
		}, //
		CHAR {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new CharAttributeType();
			}
		}, //
		DATE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new DateAttributeType();
			}
		},
		DECIMAL {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				final Integer precision = builder.precision;
				final Integer scale = builder.scale;
				return new DecimalAttributeType(precision, scale);
			}
		}, //
		DOUBLE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new DoubleAttributeType();
			}
		}, //
		FOREIGNKEY {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new ForeignKeyAttributeType(builder.fkDestinationName);
			}
		}, //
		INET {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new IpAddressAttributeType();
			}
		}, //
		INTEGER {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new IntegerAttributeType();
			}
		}, //
		LOOKUP {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				final String lookupType = builder.lookupType;
				return new LookupAttributeType(lookupType);
			}
		}, //
		REFERENCE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				final String domain = builder.domain;
				return new ReferenceAttributeType(domain);
			}
		}, //
		STRING {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				final Integer length = builder.length;
				return new StringAttributeType(length);
			}
		}, //
		TIME {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new TimeAttributeType();
			}
		}, //
		TIMESTAMP {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new DateTimeAttributeType();
			}
		}, //
		TEXT {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return new TextAttributeType();
			}
		}, //
		UNDEFINED {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeBuilder builder) {
				return UndefinedAttributeType.undefined();
			}
		}; //

		public abstract CMAttributeType<?> buildFrom(AttributeBuilder builder);

		public static AttributeTypeBuilder from(final String name) {
			for (final AttributeTypeBuilder attributeType : values()) {
				if (attributeType.name().equals(name)) {
					return attributeType;
				}
			}
			Log.CMDBUILD.warn(format("cannot find attribute type for name '%s', attribute is undefined", name));
			return UNDEFINED;
		}

	}

	private static enum Condition {

		ACTIVE, //
		DISPLAYABLE_IN_LIST, //
		HIDDEN, //
		NULL_VALUES_ALLOWED, //
		READ_ONLY, //
		UNIQUE_VALUES, //
		WRITABLE, //

	}

	public static class AttributeBuilder implements Builder<Attribute> {

		private String name;
		private String ownerName;
		private String ownerNamespace;
		private String description;
		private String group;
		private String fkDestinationName;
		private String defaultValue;
		private String typeName;
		private AttributeTypeBuilder attributeType = null;
		private CMAttributeType<?> type;
		private Integer precision;
		private Integer scale;
		private Integer length;
		private String lookupType;
		private Mode mode = Mode.WRITE;
		private int index = -1;
		private int classOrder = 0;
		private String domain;
		private String editorType;
		private String filter;
		private Map<MetadataAction, List<Metadata>> metadataByAction = Maps.newHashMap();
		private final Set<Condition> conditions;

		private AttributeBuilder() {
			// use factory method
			conditions = EnumSet.of(Condition.ACTIVE);
		}

		@Override
		public Attribute build() {
			Validate.isTrue(isNotBlank(name), "invalid name");
			Validate.notNull(ownerName, "missing owner");
			Validate.isTrue(isNotBlank(ownerName), "invalid name");
			description = defaultIfBlank(description, name);
			calculateType();
			return new Attribute(this);
		}

		private void calculateType() {
			if (attributeType == null) {
				attributeType = AttributeTypeBuilder.from(typeName);
			}

			type = attributeType.buildFrom(this);
		}

		public AttributeBuilder withName(final String name) {
			this.name = trim(name);
			return this;
		}

		public AttributeBuilder withOwnerName(final String ownerName) {
			this.ownerName = ownerName;
			return this;
		}

		public AttributeBuilder withOwnerNamespace(final String ownerNamespace) {
			this.ownerNamespace = ownerNamespace;
			return this;
		}
		
		public AttributeBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public AttributeBuilder withGroup(final String group) {
			this.group = group;
			return this;
		}

		public AttributeBuilder withDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public AttributeBuilder withForeignKeyDestinationClassName(final String fkDestinationName) {
			this.fkDestinationName = fkDestinationName;
			return this;
		}

		public AttributeBuilder thatIsDisplayableInList(final boolean isDisplayableInList) {
			addOrRemoveCondition(Condition.DISPLAYABLE_IN_LIST, isDisplayableInList);
			return this;
		}

		public AttributeBuilder thatIsMandatory(final boolean allowsNullValues) {
			addOrRemoveCondition(Condition.NULL_VALUES_ALLOWED, allowsNullValues);
			return this;
		}

		public AttributeBuilder thatIsUnique(final boolean thatIsUnique) {
			addOrRemoveCondition(Condition.UNIQUE_VALUES, thatIsUnique);
			return this;
		}

		public AttributeBuilder thatIsActive(final boolean isActive) {
			addOrRemoveCondition(Condition.ACTIVE, isActive);
			return this;
		}

		public AttributeBuilder withFilter(final String filter) {
			this.filter = filter;
			return this;
		}

		private void addOrRemoveCondition(final Condition condition, final boolean b) {
			if (b) {
				conditions.add(condition);
			} else {
				conditions.remove(condition);
			}
		}

		public AttributeBuilder withType(final String type) {
			this.typeName = type;
			return this;
		}

		public AttributeBuilder withType(final AttributeTypeBuilder attributeType) {
			this.attributeType = attributeType;
			return this;
		}

		public AttributeBuilder withPrecision(final Integer precision) {
			this.precision = precision;
			return this;
		}

		public AttributeBuilder withScale(final Integer scale) {
			this.scale = scale;
			return this;
		}

		public AttributeBuilder withLength(final Integer length) {
			this.length = length;
			return this;
		}

		public AttributeBuilder withLookupType(final String lookupType) {
			this.lookupType = lookupType;
			return this;
		}

		public AttributeBuilder withMode(final Mode mode) {
			this.mode = mode;
			return this;
		}

		public AttributeBuilder withIndex(final int index) {
			this.index = index;
			return this;
		}

		public AttributeBuilder withClassOrder(final int classOrder) {
			this.classOrder = classOrder;
			return this;
		}

		public AttributeBuilder withDomain(final String domain) {
			this.domain = domain;
			return this;
		}

		public AttributeBuilder withEditorType(final String editorType) {
			this.editorType = editorType;
			return this;
		}

		public AttributeBuilder withMetadata(final Map<MetadataAction, List<Metadata>> metadataByAction) {
			this.metadataByAction = metadataByAction;
			return this;
		}

	}

	public static AttributeBuilder newAttribute() {
		return new AttributeBuilder();
	}

	private final String name;
	private final String description;
	private final String ownerName;
	private final String ownerNamespace;
	private final String group;
	private final String fkDestinationName;
	private final CMAttributeType<?> type;
	private final String defaultValue;
	private final Mode mode;
	private int index;
	private final int classOrder;
	private final String editorType;
	private final String filter;
	private final Map<MetadataAction, List<Metadata>> metadataByAction;
	private final Set<Condition> conditions;
	private final transient String toString;

	private Attribute(final AttributeBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.ownerName = builder.ownerName;
		this.ownerNamespace = builder.ownerNamespace;
		this.group = builder.group == null ? EMPTY : builder.group;
		this.fkDestinationName = builder.fkDestinationName;
		this.type = builder.type;
		this.defaultValue = builder.defaultValue;
		this.mode = builder.mode;
		this.index = builder.index;
		this.classOrder = builder.classOrder;
		this.editorType = builder.editorType;
		this.filter = builder.filter;
		this.metadataByAction = builder.metadataByAction;
		this.conditions = builder.conditions;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getOwnerNamespace() {
		return ownerNamespace;
	}
	
	public String getGroup() {
		return group;
	}

	public CMAttributeType<?> getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isDisplayableInList() {
		return conditions.contains(Condition.DISPLAYABLE_IN_LIST);
	}

	public boolean isMandatory() {
		return conditions.contains(Condition.NULL_VALUES_ALLOWED);
	}

	public boolean isUnique() {
		return conditions.contains(Condition.UNIQUE_VALUES);
	}

	public boolean isActive() {
		return conditions.contains(Condition.ACTIVE);
	}

	public Mode getMode() {
		return mode;
	}

	public int getIndex() {
		return index;
	}

	public int setIndex(final int index) {
		return this.index = index;
	}

	public int getClassOrder() {
		return classOrder;
	}

	public String getEditorType() {
		return editorType;
	}

	public String getFilter() {
		return filter;
	}

	public String getForeignKeyDestinationClassName() {
		return fkDestinationName;
	}

	public Map<MetadataAction, List<Metadata>> getMetadata() {
		return metadataByAction;
	}

	@Override
	public String toString() {
		return toString;
	}

}
