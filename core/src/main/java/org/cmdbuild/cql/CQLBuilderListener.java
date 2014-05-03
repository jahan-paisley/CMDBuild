package org.cmdbuild.cql;

/**
 * This interface defines the method that the CQLCompilerBuilder will call when
 * parsing the CQL expression tree
 */
public interface CQLBuilderListener {

	/**
	 * Handles the case of an id which is a lookup operator (e.g.
	 * Lookup.parent())
	 */
	public static class LookupOperator {
		String operator; // only parent()
		String attributeName; // can be null

		public LookupOperator(final String o, final String a) {
			this.operator = o;
			this.attributeName = a;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public String getOperator() {
			return operator;
		}

		@Override
		public String toString() {
			return attributeName == null ? operator : attributeName + ":" + operator;
		}
	}

	/**
	 * Stores an input value (e.g. Foo = {Variable})
	 */
	public static class FieldInputValue {
		String variableName;

		public FieldInputValue(final String v) {
			this.variableName = v;
		}

		public String getVariableName() {
			return variableName;
		}

		@Override
		public String toString() {
			return "{" + variableName + "}";
		}
	}

	/**
	 * Stores a native-sql value (e.g. Foo = / select "bar" from "baz" /)
	 */
	public static class FieldNativeSQLValue {
		String sql;

		public FieldNativeSQLValue(final String q) {
			this.sql = q;
		}

		public String getSql() {
			return sql;
		}

		@Override
		public String toString() {
			return sql;
		}
	}

	/**
	 * Direction of a domain: the default value means that the direction can be
	 * auto-sensed or, in case of ambiguity, direct. <br>
	 * The inverse value is useful only for ambiguos domains.
	 */
	public enum DomainDirection {
		DEFAULT, INVERSE;
	}

	/**
	 * Defines if a where element is the First one or is in And/Or mode
	 */
	public enum WhereType {
		FIRST, AND, OR;
	}

	/**
	 * Defines the possibile operators
	 */
	public enum FieldOperator {
		LTEQ, GTEQ, LT, GT, EQ, CONT, BGN, END, BTW, IN, ISNULL;
	}

	/**
	 * Defines the value type
	 */
	public enum FieldValueType {
		BOOL, DATE, TIMESTAMP, INPUT, STRING, FLOAT, INT, SUBEXPR, NATIVE;
	}

	/**
	 * Defines the order by type
	 */
	public enum OrderByType {
		DEFAULT, ASC, DESC;
	}

	// global

	/**
	 * Called just before starting the first CQL expression analysis
	 */
	void globalStart();

	/**
	 * Called just after finishing the CQL expression analysis
	 */
	void globalEnd();

	//

	// expression

	/**
	 * Called just before starting an expression (can be a nested expression)
	 */
	void startExpression();

	/**
	 * Called just after finishing an expression
	 */
	void endExpression();

	//

	// select
	/**
	 * Called when there is no a SELECT statement
	 */
	void defaultSelect();

	/**
	 * Called before starting a select statement
	 */
	void startSelect();

	/**
	 * called if the tokens SELECT * are encountered.
	 */
	void selectAll();

	/**
	 * Called between:
	 * <ul>
	 * <li>start/end function</li>
	 * <li>start/end class</li>
	 * <li>start/end domain meta</li>
	 * <li>start/end domain objects</li>
	 * </ul>
	 * 
	 * @param attributeName
	 * @param attributeAs
	 * @param classNameOrReference
	 */
	void addSelectAttribute(String attributeName, String attributeAs, String classNameOrReference);

	/**
	 * Called when parsing a select function is called (e.g. SELECT ... , Func(
	 * Foo,Bar ) ... )
	 * 
	 * @param functionName
	 * @param functionAs
	 */
	void startSelectFunction(String functionName, String functionAs);

	/**
	 * Called after a select function parse.
	 */
	void endSelectFunction();

	/**
	 * Called before selecting from a class (e.g. SELECT Foo::(A,B,C) )
	 */
	void startSelectFromClass(String classNameOrReference);

	void endSelectFromClass();

	/**
	 * Called before selecting from a domain (e.g. SELECT
	 * Foo::meta(A,B)objects(C,D) )
	 * 
	 * @param domainNameOrReference
	 */
	void startSelectFromDomain(String domainNameOrReference);

	/**
	 * Called before selecting from a domain meta (e.g. SELECT Foo::meta(A,B,C)
	 * )
	 */
	void startSelectFromDomainMeta();

	void endSelectFromDomainMeta();

	/**
	 * Called before selecting from a domain objects (e.g. SELECT
	 * Foo::objects(A,B,C) )
	 */
	void startSelectFromDomainObjects();

	void endSelectFromDomainObjects();

	void endSelectFromDomain();

	/**
	 * Called after a SELECT statement has been analyzed.
	 */
	void endSelect();

	// from

	/**
	 * called before starting a FROM statement, e.g. FROM A or FROM HISTORY A <br>
	 * In the latter case, the parameter "history" is true.
	 * 
	 * @param history
	 */
	void startFrom(boolean history);

	/**
	 * Add a from token, defines a class name and his optional AS
	 * 
	 * @param className
	 * @param classAs
	 *            , can be null
	 */
	void addFromClass(String className, String classAs);

	/**
	 * Add a from token, defines a class oid and his optional AS
	 * 
	 * @param classId
	 * @param classAs
	 *            can be null
	 */
	void addFromClass(int classId, String classAs);

	/**
	 * Domains can be chained, so that a definition falls under the scope of the
	 * previous domain or class. When a domain definition ends, endFromDomain is
	 * called
	 * 
	 * @param scopeReference
	 * @param domainName
	 * @param inverse
	 */
	void startFromDomain(String scopeReference, String domainName, String domainAs, DomainDirection direction);

	void startFromDomain(String scopeReference, int domainId, String domainas, DomainDirection direction);

	void endFromDomain();

	void endFrom();

	//

	// where
	/**
	 * Called if there is no WHERE statement.
	 */
	void defaultWhere(); // used when there is no WHERE statement, eg. "From A":
							// select all active A

	/**
	 * Called before starting a WHERE statement
	 */
	void startWhere();

	/**
	 * Called before starting a grouped fields (e.g. WHERE (A>10|A<2) & ... )
	 * 
	 * @param type
	 * @param isNot
	 */
	void startGroup(WhereType type, boolean isNot);

	void endGroup();

	/**
	 * Called before starting a domain, defined in the WHERE statement, using
	 * the domain name (e.g. FROM [Domain].objects( A>10 ) )
	 * 
	 * @param type
	 * @param scopeReference
	 * @param domainName
	 * @param direction
	 * @param isNot
	 */
	void startDomain(WhereType type, String scopeReference, String domainName, DomainDirection direction, boolean isNot);

	/**
	 * Called before starting a domain, defined in the WHERE statement, using
	 * the domain id (e.g. FROM [101123].objects( A>10 ) )
	 * 
	 * @param type
	 * @param scopeReference
	 * @param domainId
	 * @param direction
	 * @param isNot
	 */
	void startDomain(WhereType type, String scopeReference, int domainId, DomainDirection direction, boolean isNot);

	/**
	 * Called before starting a domain, using the AS name (e.g. FROM [DomainA]
	 * AS DomA WHERE DomA.objects( A > 10 ) )
	 * 
	 * @param type
	 * @param domainReference
	 * @param isNot
	 */
	void startDomainRef(WhereType type, String domainReference, boolean isNot);

	/**
	 * Called before filtering on a domain meta rows
	 */
	void startDomainMeta();

	void endDomainMeta();

	/**
	 * Called before filtering on a domain object rows
	 */
	void startDomainObjects();

	void endDomainObjects();

	void endDomain();

	void endDomainRef();

	/**
	 * Simple fields example:
	 * <ul>
	 * <li>Code = ...</li>
	 * <li>A.Code = ... , where 'A' is referenced either as a Class or a Domain</li>
	 * </ul>
	 * 
	 * @param type
	 * @param isNot
	 * @param classOrDomainNameOrRef
	 * @param fieldId
	 * @param operator
	 */
	void startSimpleField(WhereType type, boolean isNot, String classOrDomainNameOrRef, String fieldId,
			FieldOperator operator);

	/**
	 * Navigation through reference
	 * 
	 * @param type
	 * @param isNot
	 * @param classOrDomainNameOrRef
	 * @param fieldPath
	 * @param operator
	 */
	void startComplexField(WhereType type, boolean isNot, String classOrDomainNameOrRef, String[] fieldPath,
			FieldOperator operator);

	/**
	 * Every thing that contains a 'parent()' within the id, ad es.
	 * State.parent() = 'open'
	 * 
	 * @param type
	 * @param isNot
	 * @param classOrDomainNameOrRef
	 * @param lookupPath
	 * @param operator
	 */
	void startLookupField(WhereType type, boolean isNot, String classOrDomainNameOrRef, String fieldId,
			LookupOperator[] lookupPath, FieldOperator operator);

	void startValue(FieldValueType type);

	/**
	 * Every value except for SubExpression are putted with the "value" method.
	 * Subexpression are handled calling start/end expression
	 * 
	 * @param o
	 */
	void value(Object o);

	void endValue();

	void endField();

	void endWhere();

	// group by
	/**
	 * Called if there is no GROUP BY statement, much probably this method will
	 * simply not be implemented.
	 */
	void defaultGroupBy(); // much probably unused, just in case

	void startGroupBy();

	void addGroupByElement(String classDomainReference, String attributeName);

	void endGroupBy();

	// order by
	/**
	 * Called when there is no ORDER BY statement
	 */
	void defaultOrderBy();

	void startOrderBy();

	void addOrderByElement(String classDomainReference, String attributeName, OrderByType type);

	void endOrderBy();

	// limit & offset
	/**
	 * Called when there is no LIMIT statement
	 */
	void defaultLimit();

	/**
	 * Called when there is no OFFSET statement
	 */
	void defaultOffset();

	void setLimit(int limit);

	void setLimit(FieldInputValue limit);

	void setOffset(int offset);

	void setOffset(FieldInputValue offset);
}
