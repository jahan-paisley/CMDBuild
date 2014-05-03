package org.cmdbuild.services.soap.types;

public class Query {

	private Filter filter;
	private FilterOperator filterOperator;

	public Query() {
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public FilterOperator getFilterOperator() {
		return filterOperator;
	}

	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}

}
