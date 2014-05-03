package org.cmdbuild.dao.query.clause.where;

import java.util.List;

import com.google.common.collect.Lists;

public class NotWhereClause extends CompositeWhereClause {

	private NotWhereClause(final List<? extends WhereClause> clauses) {
		super(clauses);
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

	public static WhereClause not(final WhereClause single) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(single);
		return new NotWhereClause(clauses);
	}

}
