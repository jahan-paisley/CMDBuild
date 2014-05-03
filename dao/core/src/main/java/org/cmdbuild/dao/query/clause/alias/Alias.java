package org.cmdbuild.dao.query.clause.alias;

public interface Alias {

	void accept(AliasVisitor visitor);

}
