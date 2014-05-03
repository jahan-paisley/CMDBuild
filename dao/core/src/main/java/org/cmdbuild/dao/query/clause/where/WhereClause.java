package org.cmdbuild.dao.query.clause.where;

public interface WhereClause {

	void accept(WhereClauseVisitor visitor);

}
