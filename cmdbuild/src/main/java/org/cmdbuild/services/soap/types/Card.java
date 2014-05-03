package org.cmdbuild.services.soap.types;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.logger.Log;
import org.joda.time.DateTime;

public class Card {

	public interface ValueSerializer {

		String serializeValueForAttribute(CMAttributeType<?> attributeType, String attributeName, Object attributeValue);

	}

	public static final ValueSerializer LEGACY_VALUE_SERIALIZER = new LegacyValueSerializer();
	public static final ValueSerializer HACK_VALUE_SERIALIZER = new HackValueSerializer();

	private static abstract class AbstractValueSerializer implements ValueSerializer {

		@Override
		public final String serializeValueForAttribute(final CMAttributeType<?> attributeType,
				final String attributeName, final Object attributeValue) {
			if (attributeValue == null) {
				return StringUtils.EMPTY;
			} else {
				final Object convertedValue;
				if (isLookUpReferenceOrForeignKey(attributeType)) {
					convertedValue = convertLookUpReferenceOrForeignKey(attributeValue);
				} else if (isDateTimeOrTimeStamp(attributeType)) {
					convertedValue = convertDateTimeOrTimeStamp(attributeType, attributeValue);
				} else {
					convertedValue = attributeType.convertValue(attributeValue);
				}
				return convertedValue != null ? convertedValue.toString() : StringUtils.EMPTY;
			}
		}

		protected Object convertLookUpReferenceOrForeignKey(final Object attributeValue) {
			final Object convertedValue;
			final IdAndDescription foreignReference = (IdAndDescription) attributeValue;
			convertedValue = foreignReference != null ? foreignReference.getDescription() : StringUtils.EMPTY;
			return convertedValue;
		}

		protected Object convertDateTimeOrTimeStamp(final CMAttributeType<?> attributeType, final Object attributeValue) {
			return new NullAttributeTypeVisitor() {

				private Object attributeValue;
				private Object convertedValue;

				@Override
				public void visit(final DateAttributeType attributeType) {
					convertedValue = dateAsString(attributeType.convertValue(attributeValue), dateFormat());
				}

				@Override
				public void visit(final DateTimeAttributeType attributeType) {
					convertedValue = dateAsString(attributeType.convertValue(attributeValue), dateTimeFormat());
				}

				@Override
				public void visit(final TimeAttributeType attributeType) {
					convertedValue = dateAsString(attributeType.convertValue(attributeValue), timeFormat());
				}

				private String dateAsString(final DateTime dateTime, final String dateFormat) {
					return new SimpleDateFormat(dateFormat).format(dateTime.toDate());
				}

				public Object convert(final Object attributeValue) {
					this.attributeValue = attributeValue;
					attributeType.accept(this);
					return convertedValue;
				}

			}.convert(attributeValue);
		}

		protected String dateFormat() {
			return Constants.DATE_TWO_DIGIT_YEAR_FORMAT;
		};

		protected String dateTimeFormat() {
			return Constants.DATETIME_TWO_DIGIT_YEAR_FORMAT;
		};

		protected String timeFormat() {
			return Constants.TIME_FORMAT;
		};

	}

	private static class LegacyValueSerializer extends AbstractValueSerializer {

	}

	private static class HackValueSerializer extends AbstractValueSerializer {

		@Override
		protected String dateFormat() {
			return Constants.SOAP_ALL_DATES_PRINTING_PATTERN;
		}

		@Override
		protected String dateTimeFormat() {
			return Constants.SOAP_ALL_DATES_PRINTING_PATTERN;
		}

		@Override
		protected String timeFormat() {
			return Constants.SOAP_ALL_DATES_PRINTING_PATTERN;
		}

	}

	private static boolean isLookUpReferenceOrForeignKey(final CMAttributeType<?> attributeType) {
		return attributeType instanceof ReferenceAttributeType //
				|| attributeType instanceof LookupAttributeType //
				|| attributeType instanceof ForeignKeyAttributeType;
	}

	private static boolean isDateTimeOrTimeStamp(final CMAttributeType<?> attributeType) {
		return attributeType instanceof DateAttributeType //
				|| attributeType instanceof TimeAttributeType //
				|| attributeType instanceof DateTimeAttributeType;
	}

	private String className;
	private int id;
	private Calendar beginDate;
	private Calendar endDate;
	private String user;
	private List<Attribute> attributeList;
	private List<Metadata> metadata;

	public Card() {
	}

	public Card(final org.cmdbuild.model.data.Card cardModel) {
		this(cardModel, LEGACY_VALUE_SERIALIZER);
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final ValueSerializer valueSerializer) {
		setup(cardModel);
		final List<Attribute> attrs = new ArrayList<Attribute>();
		for (final Entry<String, Object> entry : cardModel.getAttributes().entrySet()) {
			final Attribute tmpAttribute = new Attribute();
			final String attributeName = entry.getKey();
			final CMAttributeType<?> attributeType = cardModel.getType().getAttribute(attributeName).getType();
			final String value = valueSerializer.serializeValueForAttribute(attributeType, attributeName,
					cardModel.getAttribute(attributeName));
			tmpAttribute.setName(attributeName);
			tmpAttribute.setValue(value);
			if (isLookUpReferenceOrForeignKey(attributeType)) {
				final IdAndDescription foreignReference = (IdAndDescription) cardModel
						.getAttribute(attributeName);
				if (foreignReference != null && foreignReference.getId() != null) {
					tmpAttribute.setCode(foreignReference.getId().toString());
				}
			}
			attrs.add(tmpAttribute);
		}
		this.setAttributeList(attrs);
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final Attribute[] attrs,
			final ValueSerializer valueSerializer) {
		Attribute attribute;
		final List<Attribute> list = new ArrayList<Attribute>();
		Log.SOAP.debug("Filtering card with following attributes");
		for (final Attribute a : attrs) {
			final String name = a.getName();
			if (name != null && !name.equals(StringUtils.EMPTY)) {
				final Object attributeValue = cardModel.getAttribute(name);
				attribute = new Attribute();
				attribute.setName(name);
				final CMAttributeType<?> attributeType = cardModel.getType().getAttribute(name).getType();
				if (attributeValue != null) {
					attribute.setValue(valueSerializer.serializeValueForAttribute(attributeType, name, attributeValue));
				}
				if (isLookUpReferenceOrForeignKey(attributeType)) {
					final IdAndDescription foreignReference = (IdAndDescription) cardModel.getAttribute(name);
					if (foreignReference != null && foreignReference.getId() != null) {
						attribute.setCode(foreignReference.getId().toString());
					}
				}
				Log.SOAP.debug("Attribute name=" + name + ", value="
						+ valueSerializer.serializeValueForAttribute(attributeType, name, attributeValue));
				final String attributeName = attribute.getName();
				if (!attributeName.equals("Id") && !attributeName.equals("ClassName")
						&& !attributeName.equals("BeginDate") && !attributeName.equals("User")
						&& !attributeName.equals("EndDate")) {
					list.add(attribute);
				}
			}

			this.setAttributeList(list);
		}
		setup(cardModel);
	}

	public Card(final org.cmdbuild.model.data.Card cardModel, final Attribute[] attrs) {
		this(cardModel, attrs, new LegacyValueSerializer());
	}

	protected void setup(final org.cmdbuild.model.data.Card cardModel) {
		final int id = cardModel.getId().intValue();
		this.setId(id);
		this.setClassName(cardModel.getClassName());
		this.setUser(cardModel.getUser());
		this.setBeginDate(cardModel.getBeginDate().toGregorianCalendar());
		final DateTime endDate = cardModel.getEndDate();
		this.setEndDate(endDate != null ? endDate.toGregorianCalendar() : null);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String classname) {
		this.className = classname;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Calendar getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(final Calendar beginDate) {
		this.beginDate = beginDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(final Calendar endDate) {
		this.endDate = endDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(final List<Attribute> attributeList) {
		this.attributeList = attributeList;
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(final List<Metadata> metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		String attributes = "";
		final Iterator<Attribute> itr = attributeList.iterator();
		while (itr.hasNext()) {
			attributes += itr.next().toString();
		}
		return "[className: " + className + " id:" + id + " attributes: " + attributes + "]";
	}
}
