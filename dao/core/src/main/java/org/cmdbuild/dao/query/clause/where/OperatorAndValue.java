package org.cmdbuild.dao.query.clause.where;

public interface OperatorAndValue {

	void accept(OperatorAndValueVisitor visitor);

}
