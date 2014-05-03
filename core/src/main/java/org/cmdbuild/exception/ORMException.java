package org.cmdbuild.exception;

public class ORMException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final ORMExceptionType type;

	public enum ORMExceptionType {
		ORM_DBNOTCONFIGURED, //
		ORM_ILLEGAL_NAME_ERROR, //
		ORM_GENERIC_ERROR, //
		ORM_DUPLICATE_TABLE, //
		ORM_UNIQUE_VIOLATION, //
		ORM_NOT_NULL_VIOLATION, //
		ORM_CONTAINS_DATA, //
		ORM_TYPE_ERROR, //
		ORM_DATABASE_CONNECTION_ERROR, //
		ORM_CANT_CREATE_DATABASE, //
		ORM_CANT_CREATE_USER, //
		ORM_CANT_CREATE_GROUP, //
		ORM_ERROR_GENERIC_SQL, //
		ORM_ERROR_GETTING_PK, //
		ORM_ERROR_LOOKUP_CREATE, //
		ORM_ERROR_LOOKUP_MODIFY, //
		ORM_ERROR_LOOKUP_DELETE, //
		ORM_ERROR_DOMAIN_CREATE, //
		ORM_ERROR_DOMAIN_MODIFY, //
		ORM_ERROR_DOMAIN_DELETE, //
		ORM_ERROR_RELATION_CREATE, //
		ORM_ERROR_RELATION_MODIFY, //
		ORM_ERROR_CARD_SELECT, //
		ORM_ERROR_CARD_UPDATE, //
		ORM_CHANGE_LOOKUPTYPE_ERROR, //
		ORM_READ_ONLY_TABLE, //
		ORM_READ_ONLY_RELATION, //
		ORM_DUPLICATE_ATTRIBUTE, //
		ORM_DOMAIN_HAS_REFERENCE, //
		ORM_FILTER_CONFLICT, //
		ORM_AMBIGUOUS_DIRECTION, //
		ORM_DUPLICATE_USER, //
		ORM_DUPLICATE_GROUP, //
		ORM_FULLCARDS_MULTIPLEDOMAINS, //
		ORM_LOOKUPTYPE_ALREADY_EXISTS, //
		ORM_CSV_INVALID_ATTRIBUTES, //
		ORM_CANT_DELETE_CARD_WITH_RELATION, //
		ORM_ERROR_INCOMPATIBLE_CLASS, //
		ORM_MALFORMED_PATCH, //
		ORM_SQL_PATCH, //
		ORM_CQL_COMPILATION_FAILED, //
		ORM_ICONS_FILE_ALREADY_EXISTS, //
		ORM_ICONS_FILE_NOT_FOUND, //
		ORM_ICONS_UNSUPPORTED_TYPE, //
		ORM_POSTGIS_NOT_FOUND, //
		ORM_TABLE_HAS_DOMAIN, //
		ORM_TABLE_HAS_CHILDREN;

		public ORMException createException(final String... parameters) {
			return new ORMException(this, parameters);
		}
	}

	private ORMException(final ORMExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public ORMExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
