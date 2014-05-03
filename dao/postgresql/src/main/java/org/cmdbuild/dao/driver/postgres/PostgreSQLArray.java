package org.cmdbuild.dao.driver.postgres;

import static java.sql.Types.VARCHAR;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

class PostgreSQLArray implements Array {

	private static final String NULL = "NULL";
	private static final String EMPTY = "{}";

	private static final String BASE_TYPE_NAME = "text";

	private final String[] values;
	private final String value;

	public PostgreSQLArray(final String[] values) {
		this.values = values;
		this.value = stringArrayToPostgreSQLTextArray(this.values);

	}

	@Override
	public String toString() {
		return value;
	}

	public static String stringArrayToPostgreSQLTextArray(final String[] stringArray) {
		final int arrayLength;
		if (stringArray == null) {
			return NULL;
		} else if ((arrayLength = stringArray.length) == 0) {
			return EMPTY;
		}
		// count the string length and if need to quote
		int neededBufferLentgh = 2; // count the beginning '{' and the
									// ending '}' brackets
		final boolean[] shouldQuoteArray = new boolean[stringArray.length];
		for (int si = 0; si < arrayLength; si++) {
			// count the comma after the first element
			if (si > 0) {
				neededBufferLentgh++;
			}

			boolean shouldQuote;
			final String s = stringArray[si];
			if (s == null) {
				neededBufferLentgh += 4;
				shouldQuote = false;
			} else {
				final int l = s.length();
				neededBufferLentgh += l;
				if (l == 0 || s.equalsIgnoreCase(NULL)) {
					shouldQuote = true;
				} else {
					shouldQuote = false;
					// scan for commas and quotes
					for (int i = 0; i < l; i++) {
						final char ch = s.charAt(i);
						switch (ch) {
						case '"':
						case '\\':
							shouldQuote = true;
							// we will escape these characters
							neededBufferLentgh++;
							break;
						case ',':
						case '\'':
						case '{':
						case '}':
							shouldQuote = true;
							break;
						default:
							if (Character.isWhitespace(ch)) {
								shouldQuote = true;
							}
							break;
						}
					}
				}
				// count the quotes
				if (shouldQuote) {
					neededBufferLentgh += 2;
				}
			}
			shouldQuoteArray[si] = shouldQuote;
		}

		final StringBuilder sb = new StringBuilder(neededBufferLentgh);
		sb.append('{');
		for (int si = 0; si < arrayLength; si++) {
			final String s = stringArray[si];
			if (si > 0) {
				sb.append(',');
			}
			if (s == null) {
				sb.append(NULL);
			} else {
				final boolean shouldQuote = shouldQuoteArray[si];
				if (shouldQuote) {
					sb.append('"');
				}
				for (int i = 0, l = s.length(); i < l; i++) {
					final char ch = s.charAt(i);
					if (ch == '"' || ch == '\\') {
						sb.append('\\');
					}
					sb.append(ch);
				}
				if (shouldQuote) {
					sb.append('"');
				}
			}
		}
		sb.append('}');
		assert sb.length() == neededBufferLentgh;
		return sb.toString();
	}

	@Override
	public Object getArray() throws SQLException {
		return values == null ? null : Arrays.copyOf(values, values.length);
	}

	@Override
	public Object getArray(final Map<String, Class<?>> map) throws SQLException {
		return getArray();
	}

	@Override
	public Object getArray(final long index, final int count) throws SQLException {
		return values == null ? null : Arrays.copyOfRange(values, (int) index, (int) index + count);
	}

	@Override
	public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
		return getArray(index, count);
	}

	@Override
	public int getBaseType() throws SQLException {
		return VARCHAR;
	}

	@Override
	public String getBaseTypeName() throws SQLException {
		return BASE_TYPE_NAME;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(final long index, final int count) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map)
			throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() throws SQLException {
		// nothing to do
	}

}
