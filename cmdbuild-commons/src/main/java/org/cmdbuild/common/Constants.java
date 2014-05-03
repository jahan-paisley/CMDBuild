package org.cmdbuild.common;

public interface Constants {

	final String BASE_CLASS_NAME = "Class";
	final String BASE_PROCESS_CLASS_NAME = "Activity";
	final String LOOKUP_CLASS_NAME = "LookUp";
	final String ROLE_CLASS_NAME = "Role";

	final String CODE_ATTRIBUTE = "Code";
	final String DESCRIPTION_ATTRIBUTE = "Description";

	final String ID_ATTRIBUTE = "Id";
	final String CLASS_ID_ATTRIBUTE = "IdClass";

	final String DATETIME_FOUR_DIGIT_YEAR_FORMAT = "dd/MM/yyyy HH:mm:ss";
	final String DATETIME_TWO_DIGIT_YEAR_FORMAT = "dd/MM/yy HH:mm:ss";
	final String DATETIME_PRINTING_PATTERN = DATETIME_FOUR_DIGIT_YEAR_FORMAT;
	final String DATETIME_PARSING_PATTERN = DATETIME_TWO_DIGIT_YEAR_FORMAT;

	final String TIME_FORMAT = "HH:mm:ss";
	final String TIME_PRINTING_PATTERN = TIME_FORMAT;
	final String TIME_PARSING_PATTERN = TIME_FORMAT;

	final String DATE_FOUR_DIGIT_YEAR_FORMAT = "dd/MM/yyyy";
	final String DATE_TWO_DIGIT_YEAR_FORMAT = "dd/MM/yy";

	final String DATE_PRINTING_PATTERN = DATE_FOUR_DIGIT_YEAR_FORMAT;
	/**
	 * The two-digit pattern accepts four digits as well! The four-digit one
	 * would interpret 12 as 0012 instead of 2012!
	 */
	final String DATE_PARSING_PATTERN = DATE_TWO_DIGIT_YEAR_FORMAT;

	final String SOAP_ALL_DATES_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	final String SOAP_ALL_DATES_PRINTING_PATTERN = SOAP_ALL_DATES_FORMAT;
	final String SOAP_ALL_DATES_PARSING_PATTERN = SOAP_ALL_DATES_FORMAT;

	interface Webservices {

		final String BOOLEAN_TYPE_NAME = "BOOLEAN";
		final String CHAR_TYPE_NAME = "CHAR";
		final String DATE_TYPE_NAME = "DATE";
		final String DECIMAL_TYPE_NAME = "DECIMAL";
		final String DOUBLE_TYPE_NAME = "DOUBLE";
		final String FOREIGNKEY_TYPE_NAME = "FOREIGNKEY";
		final String INET_TYPE_NAME = "INET";
		final String INTEGER_TYPE_NAME = "INTEGER";
		final String LOOKUP_TYPE_NAME = "LOOKUP";
		final String REFERENCE_TYPE_NAME = "REFERENCE";
		final String STRING_TYPE_NAME = "STRING";
		final String TEXT_TYPE_NAME = "TEXT";
		final String TIMESTAMP_TYPE_NAME = "TIMESTAMP";
		final String TIME_TYPE_NAME = "TIME";
		final String UNKNOWN_TYPE_NAME = "UNKNOWN";

	}

}
