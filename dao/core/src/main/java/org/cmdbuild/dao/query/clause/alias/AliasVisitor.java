package org.cmdbuild.dao.query.clause.alias;

public interface AliasVisitor {

	void visit(EntryTypeAlias alias);

	void visit(NameAlias alias);

}
