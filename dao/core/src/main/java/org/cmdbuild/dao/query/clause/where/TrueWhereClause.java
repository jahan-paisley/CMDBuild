package org.cmdbuild.dao.query.clause.where;

public class TrueWhereClause implements WhereClause {

	private static final TrueWhereClause INSTANCE = new TrueWhereClause();

	private TrueWhereClause() {
		// prevents instantiation
	}

	public static TrueWhereClause trueWhereClause() {
		return INSTANCE;
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

}
