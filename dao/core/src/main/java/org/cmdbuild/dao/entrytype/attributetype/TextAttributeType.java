package org.cmdbuild.dao.entrytype.attributetype;

public class TextAttributeType extends AbstractTextAttributeType {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

}
