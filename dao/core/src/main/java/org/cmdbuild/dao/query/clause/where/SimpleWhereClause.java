package org.cmdbuild.dao.query.clause.where;

import org.cmdbuild.dao.query.clause.QueryAliasAttribute;

public class SimpleWhereClause implements WhereClause {

	private final QueryAliasAttribute attribute;
	private final OperatorAndValue operator;
	private String attributeNameCast;

	private SimpleWhereClause(final QueryAliasAttribute attribute, final OperatorAndValue operator) {
		this.attribute = attribute;
		this.operator = operator;
	}

	public QueryAliasAttribute getAttribute() {
		return attribute;
	}

	public OperatorAndValue getOperator() {
		return operator;
	}

	public String getAttributeNameCast() {
		return attributeNameCast;
	}

	public void setAttributeNameCast(final String cast) {
		attributeNameCast = cast;
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);
	}

	public static WhereClause condition(final QueryAliasAttribute attribute, final OperatorAndValue operator) {
		final SimpleWhereClause swc = new SimpleWhereClause(attribute, operator);
		swc.setAttributeNameCast(null);
		return swc;
	}

}
