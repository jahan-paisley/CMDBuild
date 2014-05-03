package org.cmdbuild.dao.query.clause.where;

public class FalseWhereClause implements WhereClause {

	private static final FalseWhereClause INSTANCE = new FalseWhereClause();

	private FalseWhereClause() {
		// prevents instantiation
	}

	public static FalseWhereClause falseWhereClause() {
		return INSTANCE;
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}
