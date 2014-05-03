package org.cmdbuild.services.soap.types;

import java.util.List;

public class FilterOperator {

	private String operator;
	private List<Query> subquery;

	public FilterOperator() {
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<Query> getSubquery() {
		return subquery;
	}

	public void setSubquery(List<Query> subquery) {
		this.subquery = subquery;
	}

}
