package org.cmdbuild.services.soap.serializer;

import static java.lang.String.format;

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
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.servlets.json.util.AttributeType;

public class AttributeSchemaSerializer implements CMAttributeTypeVisitor {

	private AttributeSchema serialized;

	public static AttributeSchema serialize(final CMFunction.CMFunctionParameter functionParameter) {
		final AttributeSchemaSerializer serializer = new AttributeSchemaSerializer();
		serializer.serialized = new AttributeSchema();
		serializer.serialized.setName(functionParameter.getName());
		functionParameter.getType().accept(serializer);
		return serializer.serialized;
	}

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		setType(AttributeType.BOOLEAN);
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		setType(AttributeType.CHAR);
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		setType(AttributeType.TIMESTAMP);
	}

	@Override
	public void visit(final DateAttributeType attributeType) {
		setType(AttributeType.DATE);
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		setType(AttributeType.DECIMAL);
		if (attributeType.precision != null) {
			serialized.setPrecision(attributeType.precision);
		}
		if (attributeType.scale != null) {
			serialized.setScale(attributeType.scale);
		}
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		setType(AttributeType.DOUBLE);
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		throwIllegalType(attributeType);
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		setType(AttributeType.FOREIGNKEY);
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		setType(AttributeType.INTEGER);
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		setType(AttributeType.INET);
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		throwIllegalType(attributeType);
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		throwIllegalType(attributeType);
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		setType(AttributeType.STRING);
		if (attributeType.length != null) {
			serialized.setLength(attributeType.length);
		}
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		setType(AttributeType.TEXT);
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		setType(AttributeType.TIME);
	}

	private void setType(final AttributeType attributeType) {
		serialized.setType(attributeType.name());
	}

	private void throwIllegalType(final CMAttributeType<?> attributeType) {
		final String message = format("'%s' not supported", attributeType.getClass());
		throw new IllegalArgumentException(message);
	}

	@Override
	public void visit(final StringArrayAttributeType stringArrayAttributeType) {
		setType(AttributeType.STRINGARRAY);
	}

}
