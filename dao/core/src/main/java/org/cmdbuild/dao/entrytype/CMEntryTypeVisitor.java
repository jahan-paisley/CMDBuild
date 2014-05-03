package org.cmdbuild.dao.entrytype;

public interface CMEntryTypeVisitor {

	void visit(CMClass type);

	void visit(CMDomain type);

	void visit(CMFunctionCall type);

}
