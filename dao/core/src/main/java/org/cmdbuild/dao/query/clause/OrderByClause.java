package org.cmdbuild.dao.query.clause;

public class OrderByClause {

	public static enum Direction {

		ASC, //
		DESC, //
		;

	}

	private final QueryAliasAttribute attribute;
	private final Direction direction;

	public OrderByClause(final QueryAliasAttribute attribute, final Direction direction) {
		this.attribute = attribute;
		this.direction = direction;
	}

	public QueryAliasAttribute getAttribute() {
		return attribute;
	}

	public Direction getDirection() {
		return direction;
	}

}
