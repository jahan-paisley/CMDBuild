package org.cmdbuild.dao.query.clause.where;

public interface WhereClauseVisitor {

	void visit(AndWhereClause whereClause);

	void visit(EmptyWhereClause whereClause);

	void visit(FalseWhereClause whereClause);

	void visit(FunctionWhereClause whereClause);

	void visit(NotWhereClause whereClause);

	void visit(OrWhereClause whereClause);

	void visit(SimpleWhereClause whereClause);

	void visit(TrueWhereClause whereClause);

}
