package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.joda.time.DateTime;

public abstract class AbstractAttributeValueVisitor implements CMAttributeTypeVisitor {

	protected final Object value;
	protected final CMAttributeType<?> type;

	protected Object convertedValue;

	public AbstractAttributeValueVisitor(final CMAttributeType<?> type, final Object value) {
		this.value = value;
		this.type = type;
		this.convertedValue = null;
	}

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		if (value != null) {
			convertedValue = JavaToJSONValueConverter.DATE_TIME_FORMATTER.print((DateTime) value);
		}
	}

	@Override
	public void visit(final DateAttributeType attributeType) {
		if (value != null) {
			convertedValue = JavaToJSONValueConverter.DATE_FORMATTER.print((DateTime) value);
		}
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		if (value != null) {
			convertedValue = JavaToJSONValueConverter.TIME_FORMATTER.print((DateTime) value);
		}
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		if (value instanceof IdAndDescription) {
			final IdAndDescription cardReference = IdAndDescription.class.cast(value);
			convertedValue = cardReference.getDescription();
		} else {
			convertedValue = value;
		}
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final StringArrayAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		convertedValue = value;
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		convertedValue = value;
	}

	public Object convertValue() {
		type.accept(this);
		return convertedValue;
	}

}
