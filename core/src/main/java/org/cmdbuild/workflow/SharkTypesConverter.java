package org.cmdbuild.workflow;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.String.format;
import static org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.cmdbuild.common.Builder;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SharkTypesConverter implements WorkflowTypesConverter {

	private static final Marker marker = MarkerFactory.getMarker(SharkTypesConverter.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	public static class SharkTypesConverterBuilder implements Builder<SharkTypesConverter> {

		private CMDataView dataView;
		private LookupStore lookupStore;

		@Override
		public SharkTypesConverter build() {
			return new SharkTypesConverter(this);
		}

		public SharkTypesConverterBuilder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public void setDataView(final CMDataView dataView) {
			this.dataView = dataView;
		}

		public SharkTypesConverterBuilder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public void setLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
		}

	}

	public static SharkTypesConverterBuilder newInstance() {
		return new SharkTypesConverterBuilder();
	}

	private class ToSharkTypesConverter implements CMAttributeTypeVisitor {

		private final Object input;
		private Object output;

		private ToSharkTypesConverter(final Object input) {
			this.input = input;
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultBoolean();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDouble();
			} else {
				output = BigDecimal.class.cast(input).doubleValue();
			}
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDouble();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			notifyIllegalType(attributeType);
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			throwIllegalType(attributeType);
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultInteger();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input.toString();
			}
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			output = (input == null) ? null : convertLookup(attributeType.convertValue(input).getId());
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			output = (input == null) ? null : convertReference(attributeType.convertValue(input).getId());
		}

		@Override
		public void visit(final StringArrayAttributeType attributeType) {
			notifyIllegalType(attributeType);
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultString();
			} else {
				output = input;
			}
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			if (input == null) {
				output = SharkTypeDefaults.defaultDate();
			} else {
				output = convertDateTime(input);
			}
		}

		private void notifyIllegalType(final CMAttributeType<?> attributeType) {
			logger.warn(marker, illegalTypeMessage(attributeType));
		}

		private void throwIllegalType(final CMAttributeType<?> attributeType) {
			throw new IllegalArgumentException(illegalTypeMessage(attributeType));
		}

		private String illegalTypeMessage(final CMAttributeType<?> attributeType) {
			return format("cannot send a '%s' to Shark", attributeType.getClass());
		}

	}

	private final IntegerAttributeType ID_TYPE = new IntegerAttributeType();

	private final CMDataView dataView;
	private final LookupStore lookupStore;

	private SharkTypesConverter(final SharkTypesConverterBuilder builder) {
		this.dataView = builder.dataView;
		this.lookupStore = builder.lookupStore;
	}

	@Override
	public Object fromWorkflowType(final Object value) {
		if (value instanceof LookupType) {
			final LookupType lookupType = LookupType.class.cast(value);
			return lookupType.checkValidity() ? Long.valueOf(lookupType.getId()) : null;
		} else if (value instanceof ReferenceType) {
			final ReferenceType refeference = ReferenceType.class.cast(value);
			return refeference.checkValidity() ? Long.valueOf(refeference.getId()) : null;
		} else {
			return value;
		}
	}

	@Override
	public Object toWorkflowType(final CMAttributeType<?> attributeType, final Object obj) {
		if (attributeType != null) {
			final Object value;
			if (obj instanceof Lookup) {
				value = Lookup.class.cast(obj).getId();
			} else if (obj instanceof Reference) {
				value = Reference.class.cast(obj).getId();
			} else {
				value = obj;
			}
			return convertCMDBuildVariable(attributeType, attributeType.convertValue(value));
		} else if (obj != null) {
			return convertSharkOnlyVariable(obj);
		} else {
			return null;
		}
	}

	private Object convertCMDBuildVariable(final CMAttributeType<?> attributeType, final Object obj) {
		final ToSharkTypesConverter converter = new ToSharkTypesConverter(obj);
		attributeType.accept(converter);
		return converter.output;
	}

	/**
	 * Tries to convert the values that are present only in Shark, so the
	 * attributeType is null. We can only guess the type when the value is not
	 * null.
	 * 
	 * @param native value
	 * @return shark value
	 */
	private Object convertSharkOnlyVariable(final Object obj) {
		if (obj instanceof Integer) {
			return Integer.class.cast(obj).longValue();
		} else if (obj instanceof DateTime) {
			return convertDateTime(obj);
		} else if (obj instanceof BigDecimal) {
			return BigDecimal.class.cast(obj).doubleValue();
		} else if (obj instanceof Lookup) {
			final Lookup lookup = Lookup.class.cast(obj);
			return convertLookup(lookup);
		} else if (obj instanceof Reference) {
			final Reference reference = Reference.class.cast(obj);
			return convertReference(reference);
		} else if (obj instanceof Reference[]) {
			final Reference[] references = Reference[].class.cast(obj);
			return convertReferenceArray(references);
		} else {
			return obj;
		}
	}

	private Object convertDateTime(final Object obj) {
		final long instant = DateTime.class.cast(obj).getMillis();
		return new Date(instant);
	}

	private LookupType convertLookup(final Lookup lookup) {
		return (lookup == null) ? SharkTypeDefaults.defaultLookup() : convertLookup(lookup.getId());
	}

	private LookupType convertLookup(final Long id) {
		logger.debug("getting lookup with id '{}'", id);
		if (id == null) {
			return SharkTypeDefaults.defaultLookup();
		}
		try {
			final org.cmdbuild.data.store.lookup.Lookup lookupFromStore = lookupStore.read(new Storable() {
				@Override
				public String getIdentifier() {
					return Long.toString(id);
				}
			});
			final LookupType lookupType = new LookupType();
			lookupType.setType(lookupFromStore.type.name);
			lookupType.setId(objectIdToInt(lookupFromStore.getId()));
			lookupType.setCode(lookupFromStore.code);
			lookupType.setDescription(lookupFromStore.description);
			return lookupType;
		} catch (final Exception e) {
			logger.error("cannot get lookup", e);
			return SharkTypeDefaults.defaultLookup();
		}
	}

	private ReferenceType[] convertReferenceArray(final Reference[] references) {
		final List<ReferenceType> referenceTypes = newArrayListWithCapacity(references.length);
		for (final Reference reference : references) {
			referenceTypes.add(convertReference(reference));
		}
		return referenceTypes.toArray(new ReferenceType[referenceTypes.size()]);
	}

	private ReferenceType convertReference(final Reference reference) {
		return convertReference(reference.getId(), reference.getClassName());
	}

	private ReferenceType convertReference(final Long id) {
		return convertReference(id, null);
	}

	private ReferenceType convertReference(final Long id, final String className) {
		if (id == null) {
			return SharkTypeDefaults.defaultReference();
		}
		try {
			// TODO improve performances
			final String _className = (className == null) ? Constants.BASE_CLASS_NAME : className;
			final CMClass queryClass = dataView.findClass(_className);
			final CMCard card = dataView.select(attribute(queryClass, DESCRIPTION_ATTRIBUTE)) //
					.from(queryClass) //
					.where(condition(attribute(queryClass, ID_ATTRIBUTE), eq(id))) //
					.run() //
					.getOnlyRow() //
					.getCard(queryClass);
			final ReferenceType referenceType = new ReferenceType();
			referenceType.setId(objectIdToInt(card.getId()));
			referenceType.setIdClass(objectIdToInt(card.getType().getId()));
			referenceType.setDescription(String.class.cast(card.getDescription()));
			return referenceType;
		} catch (final Exception e) {
			logger.error("cannot get reference", e);
			return SharkTypeDefaults.defaultReference();
		}
	}

	/**
	 * Converts an object identifier to the integer representation or -1 if it
	 * is null (YEAH!)
	 * 
	 * @return legacy id standard
	 */
	private int objectIdToInt(final Long objId) {
		final Integer id = ID_TYPE.convertValue(objId);
		if (id == null) {
			return -1;
		} else {
			return id;
		}
	}
}
