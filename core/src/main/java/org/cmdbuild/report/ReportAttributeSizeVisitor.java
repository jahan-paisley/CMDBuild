package org.cmdbuild.report;

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

public class ReportAttributeSizeVisitor implements CMAttributeTypeVisitor {

	private int size = 0;

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		size = 4;
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		size = 4;
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		size = 16;
	}

	@Override
	public void visit(final DateAttributeType attributeType) {
		size = 10;
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		size = 8;
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		size = 8;
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		size = 8;
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		final Integer l = attributeType.length;
		size = (l > 4 ? l : 4) > 40 ? 40 : (l > 4 ? l : 4);
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		size = 50;
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		size = 20;
	}

	@Override
	public void visit(final StringArrayAttributeType stringArrayAttributeType) {
		size = 20;
	}

	public int getSize(final CMAttributeType<?> attributeType) {
		attributeType.accept(this);
		return size;
	}

}
