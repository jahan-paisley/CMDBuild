package org.cmdbuild.dao.entrytype;

public class NullEntryTypeVisitor implements CMEntryTypeVisitor {

	@Override
	public void visit(final CMClass type) {
		// nothing to do
	}

	@Override
	public void visit(final CMDomain type) {
		// nothing to do
	}

	@Override
	public void visit(final CMFunctionCall type) {
		// nothing to do
	}

}
