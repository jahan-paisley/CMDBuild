package org.cmdbuild.api.fluent;

public class RelationsQuery {

	private final String className;
	private final int id;

	private String domainName;

	public RelationsQuery(final String className, final int id) {
		this.className = className;
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public int getCardId() {
		return id;
	}

	public String getDomainName() {
		return domainName;
	}

	void setDomainName(final String domainName) {
		this.domainName = domainName;
	}

}