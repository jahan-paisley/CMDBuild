package org.cmdbuild.dao.entrytype.attributetype;

public interface CMAttributeTypeVisitor {

	void visit(BooleanAttributeType attributeType);

	void visit(CharAttributeType attributeType);

	void visit(DateAttributeType attributeType);

	void visit(DateTimeAttributeType attributeType);

	void visit(DecimalAttributeType attributeType);

	void visit(DoubleAttributeType attributeType);

	void visit(EntryTypeAttributeType attributeType);

	void visit(ForeignKeyAttributeType attributeType);

	void visit(IntegerAttributeType attributeType);

	void visit(IpAddressAttributeType attributeType);

	void visit(LookupAttributeType attributeType);

	void visit(ReferenceAttributeType attributeType);

	void visit(StringArrayAttributeType attributeType);

	void visit(StringAttributeType attributeType);

	void visit(TextAttributeType attributeType);

	void visit(TimeAttributeType attributeType);

}
