package org.cmdbuild.report;

import java.sql.Timestamp;
import java.util.Date;

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

public class ReportAttributeTypeClassMapperVisitor implements CMAttributeTypeVisitor {
	private Class<?> javaClassForAttribute = null;

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		javaClassForAttribute = Boolean.class;
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		javaClassForAttribute = Character.class;
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		javaClassForAttribute = Timestamp.class;
	}

	@Override
	public void visit(final DateAttributeType attributeType) {
		javaClassForAttribute = Date.class;
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		javaClassForAttribute = Double.class;
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		javaClassForAttribute = Double.class;
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		javaClassForAttribute = Integer.class;
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		javaClassForAttribute = String.class;
	}

	@Override
	public void visit(final StringArrayAttributeType stringArrayAttributeType) {
		javaClassForAttribute = String[].class;
	}

	public Class<?> get(final CMAttributeType<?> attributeType) {
		attributeType.accept(this);
		return javaClassForAttribute;
	}
}
