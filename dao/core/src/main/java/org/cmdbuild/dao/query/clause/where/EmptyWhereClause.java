package org.cmdbuild.dao.query.clause.where;

public class EmptyWhereClause implements WhereClause {

	private static final EmptyWhereClause INSTANCE = new EmptyWhereClause();

	public static EmptyWhereClause getInstance() {
		return INSTANCE;
	}

	public static EmptyWhereClause emptyWhereClause() {
		return INSTANCE;
	}

	private EmptyWhereClause() {
		// prevents instantiation
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}
}
