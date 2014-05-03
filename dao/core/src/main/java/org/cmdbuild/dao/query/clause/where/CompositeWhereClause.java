package org.cmdbuild.dao.query.clause.where;

import java.util.List;

public abstract class CompositeWhereClause implements WhereClause {

	private final List<? extends WhereClause> clauses;

	public CompositeWhereClause(final List<? extends WhereClause> clauses) {
		this.clauses = clauses;
	}

	public List<? extends WhereClause> getClauses() {
		return clauses;
	}

}
