package org.cmdbuild.dao.driver.postgres;

public interface Const {
	/*
	 * FIXME We should use generic identifiers as _Code instead of Code directly
	 */

	@Deprecated
	static final String CODE_ATTRIBUTE = SystemAttributes.Code.getDBName();
	@Deprecated
	static final String DESCRIPTION_ATTRIBUTE = SystemAttributes.Description.getDBName();
	@Deprecated
	static final String ID_ATTRIBUTE = SystemAttributes.Id.getDBName();

	/*
	 * Constants
	 */

	enum SystemAttributes {
		Id("Id"), //
		IdClass("IdClass", SqlType.regclass.sqlCast()), //
		ClassId1("IdClass1", SqlType.regclass.sqlCast()), //
		ClassId2("IdClass2", SqlType.regclass.sqlCast()), //
		DomainId("IdDomain", SqlType.regclass.sqlCast()), //
		DomainId1("IdObj1"), //
		DomainId2("IdObj2"), //
		Code("Code"), //
		Description("Description"), //
		Notes("Notes"), //
		BeginDate("BeginDate"), //
		EndDate("EndDate"), //
		User("User"), //
		Status("Status"),
		// Fake attributes
		DomainQuerySource("_Src"), //
		DomainQueryTargetId("_DstId"), //
		RowNumber("_Row"), //
		RowsCount("_RowsCount"), //
		;

		final String dbName;
		final String castSuffix;

		SystemAttributes(final String dbName, final String typeCast) {
			this.dbName = dbName;
			this.castSuffix = typeCast;
		}

		SystemAttributes(final String dbName) {
			this(dbName, null);
		}

		public String getDBName() {
			return dbName;
		}

		public String getCastSuffix() {
			return castSuffix;
		}
	}

	static final Object NULL = "NULL";

	static final String OPERATOR_EQ = "=";
	static final String OPERATOR_LT = "<";
	static final String OPERATOR_GT = ">";
	static final String OPERATOR_LIKE = "ILIKE";
	static final String OPERATOR_NULL = "IS NULL";
	static final String OPERATOR_IN = "IN";

	static final String DOMAIN_PREFIX = "Map_";
	static final String HISTORY_SUFFIX = "_history";
}
