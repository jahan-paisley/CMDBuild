package org.cmdbuild.services.soap.types;

import java.util.List;

public class CQLQuery {
	
	private String cqlQuery;
	private List<CQLParameter> parameters;

	public CQLQuery() { }

	public String getCqlQuery() {
		return cqlQuery;
	}

	public void setCqlQuery(String cqlQuery) {
		this.cqlQuery = cqlQuery;
	}

	public List<CQLParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<CQLParameter> parameters) {
		this.parameters = parameters;
	}
}
