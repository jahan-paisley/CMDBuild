package org.cmdbuild.servlets.json.serializers;

import static org.joda.time.format.DateTimeFormat.forPattern;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.IdAndDescription;
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
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public class JavaToJSONValueConverter implements CMAttributeTypeVisitor {
	// TODO should be defined in the user session
	public static final DateTimeFormatter DATE_TIME_FORMATTER = forPattern(Constants.DATETIME_PRINTING_PATTERN);
	public static final DateTimeFormatter TIME_FORMATTER = forPattern(Constants.TIME_PRINTING_PATTERN);
	public static final DateTimeFormatter DATE_FORMATTER = forPattern(Constants.DATE_PRINTING_PATTERN);

	private Object valueForJson;
	private final CMAttributeType<?> type;
	private final Object value;
	private final boolean cardReferenceWithIdAndDescription;

	public JavaToJSONValueConverter( //
			final CMAttributeType<?> type, //
			final Object value //
		) {

		this(type, value, false);
	}

	public JavaToJSONValueConverter( //
			final CMAttributeType<?> type, //
			final Object value, //
			final boolean cardReferenceWithIdAndDescription //
		) {

		this.type = type;
		this.value = value;
		this.cardReferenceWithIdAndDescription = cardReferenceWithIdAndDescription;
	}

	/**
	 * Return a JSON serialization
	 * for the CMDBuild attribute values,
	 * taking care of serialization of
	 * CardReferences
	 * 
	 * @return
	 */
	public Object valueForJson() {
		type.accept(this);
		return valueForJson;
	}

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		valueForJson = value;
	}
	
	@Override
	public void visit(final DateAttributeType attributeType) {
		if (value != null) {
			valueForJson = DATE_FORMATTER.print(attributeType.convertValue(value));
		}
	}
	
	@Override
	public void visit(final TimeAttributeType attributeType) {
		if (value != null) {
			valueForJson = TIME_FORMATTER.print(attributeType.convertValue(value));
		}
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		if (value != null) {
			valueForJson = DATE_TIME_FORMATTER.print(attributeType.convertValue(value));
		}
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final StringArrayAttributeType stringArrayAttributeType) {
		valueForJson = value;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		tryToConverCardReference();
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		tryToConverCardReference();
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		tryToConverCardReference();
	}

	private void tryToConverCardReference() {
		if (value instanceof IdAndDescription) {
			final IdAndDescription cardReference = (IdAndDescription) value;
			if (cardReferenceWithIdAndDescription) {
				try {
					valueForJson = new JSONObject() {{
						put("id", cardReference.getId());
						put("description", cardReference.getDescription());
					}};
				} catch (JSONException e) {
					valueForJson = new JSONObject();
				}
			} else {
				valueForJson = cardReference.getDescription();
			}
		} else {
			valueForJson = value;
		}
	}
}
