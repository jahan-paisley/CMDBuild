package org.cmdbuild.cql.compiler.from;

import org.cmdbuild.cql.CQLBuilderListener.DomainDirection;

public interface DomainDeclaration extends FromElement {

	void setDirection(DomainDirection direction);

	void setName(String domainName);

	void setId(int domainId);

	void setAs(String domainAs);

	void setSubdomain(DomainDeclaration subdomain);

	DomainDirection getDirection();

	String getName();

	int getId();

	String getAs();

	<T extends DomainDeclaration> T getSubdomain();

	<T extends DomainDeclaration> T searchDomain(String nameOrRef);

}
