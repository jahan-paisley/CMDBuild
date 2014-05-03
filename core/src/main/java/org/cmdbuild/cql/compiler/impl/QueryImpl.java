package org.cmdbuild.cql.compiler.impl;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;
import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.Limit;
import org.cmdbuild.cql.compiler.Offset;
import org.cmdbuild.cql.compiler.OrderBy;
import org.cmdbuild.cql.compiler.Query;
import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.Where;

public class QueryImpl extends CQLElementImpl implements Query, Limit, Offset {
	private FromImpl from;
	private SelectImpl select;
	private WhereImpl where;

	private OrderByImpl orderBy;
	private GroupByImpl groupBy;

	private int literalOffset;
	private String variableOffset = null;

	private int literalLimit;
	private String variableLimit = null;

	@Override
	public FromImpl getFrom() {
		return from;
	}

	@Override
	public GroupByImpl getGroupBy() {
		return groupBy;
	}

	@Override
	public OrderByImpl getOrderBy() {
		return orderBy;
	}

	@Override
	public Limit getLimit() {
		return this;
	}

	@Override
	public Offset getOffset() {
		return this;
	}

	@Override
	public SelectImpl getSelect() {
		return select;
	}

	@Override
	public WhereImpl getWhere() {
		return where;
	}

	@Override
	public void setFrom(final From from) {
		this.from = (FromImpl) from;
	}

	@Override
	public void setGroupBy(final GroupBy groupBy) {
		this.groupBy = (GroupByImpl) groupBy;
	}

	@Override
	public void setOrderBy(final OrderBy orderBy) {
		this.orderBy = (OrderByImpl) orderBy;
	}

	@Override
	public void setSelect(final Select select) {
		this.select = (SelectImpl) select;
	}

	@Override
	public void setWhere(final Where where) {
		this.where = (WhereImpl) where;
	}

	@Override
	public void setLimit(final int limit) {
		this.literalLimit = limit;
	}

	@Override
	public void setLimit(final FieldInputValue limit) {
		setLimit(-1);
		this.variableLimit = limit.getVariableName();
	}

	@Override
	public void setOffset(final int offset) {
		this.literalOffset = offset;
	}

	@Override
	public void setOffset(final FieldInputValue offset) {
		setOffset(-1);
		this.variableOffset = offset.getVariableName();
	}

	@Override
	public Object getOffsetValue() {
		return (this.variableOffset == null) ? this.literalOffset : this.variableOffset;
	}

	@Override
	public Object getLimitValue() {
		return (this.variableLimit == null) ? this.literalLimit : this.variableLimit;
	}

	@Override
	public void setLimit(final Limit limit) {
	}

	@Override
	public void setOffet(final Offset offset) {
	}

	public void check() {
		getFrom().check();
	}

	private final Set<FieldInputValue> variableValues = new HashSet<FieldInputValue>();

	@Override
	public Set<FieldInputValue> getVariables() {
		return variableValues;
	}

	@Override
	public void addVariable(final FieldInputValue variable) {
		variableValues.add(variable);
	}
}
