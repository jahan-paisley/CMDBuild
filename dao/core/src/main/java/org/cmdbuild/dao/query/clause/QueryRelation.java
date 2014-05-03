package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entry.CMRelation;

public class QueryRelation {

	final CMRelation relation;
	final String querySource;

	private QueryRelation(final CMRelation relation, final String querySource) {
		this.relation = relation;
		this.querySource = querySource;
	}

	public CMRelation getRelation() {
		return relation;
	}

	public QueryDomain getQueryDomain() {
		return new QueryDomain(relation.getType(), querySource);
	}

	public static QueryRelation newInstance(final CMRelation relation, final String querySource) {
		return new QueryRelation(relation, querySource);
	}

}
