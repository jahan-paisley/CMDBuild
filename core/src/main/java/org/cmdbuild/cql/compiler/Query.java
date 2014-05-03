package org.cmdbuild.cql.compiler;

import java.util.Set;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;

public interface Query extends CQLElement {

	void setFrom(From from);

	void setSelect(Select select);

	void setWhere(Where where);

	void setOrderBy(OrderBy orderBy);

	void setGroupBy(GroupBy groupBy);

	void setLimit(Limit limit);

	void setOffet(Offset offset);

	From getFrom();

	Select getSelect();

	Where getWhere();

	OrderBy getOrderBy();

	GroupBy getGroupBy();

	Limit getLimit();

	Offset getOffset();

	void addVariable(FieldInputValue var);

	/**
	 * Get the variables required by this query
	 * 
	 * @return
	 */
	Set<FieldInputValue> getVariables();
}
