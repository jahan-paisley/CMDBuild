package org.cmdbuild.services.store.report;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory.ReportType;

public class ReportQuery {

	private static final String FIND_TYPES_TEMPLATE = "SELECT DISTINCT \"Type\" FROM \"Report\" WHERE \"Status\"='A';";

	private static final String LIST_BY_TYPE_TEMPLATE = "SELECT * FROM \"Report\" WHERE \"Status\" = 'A' AND \"Type\" = ?;";

	private static final String SELECT_BY_ID_TEMPLATE = "SELECT * FROM \"Report\" WHERE \"Status\" = 'A' AND \"Id\" = ?;";

	private static final String DELETE_QUERY_TEMPLATE = "DELETE FROM \"Report\" WHERE \"Id\" = ?";

	private static final String INSERT_TEMPLATE = "INSERT INTO \"Report\"(" + "\"IdClass\"," + "\"Code\","
			+ "\"Description\"," + "\"Status\"," + "\"User\"," + "\"Type\"," + "\"Query\"," + "\"SimpleReport\","
			+ "\"RichReport\"," + "\"Wizard\"," + "\"Images\"," + "\"ReportLength\"," + "\"ImagesLength\","
			+ "\"Groups\"," + "\"ImagesName\")" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,"
			+ "cast(string_to_array('%s',',') as int[])," + "cast(string_to_array('%s',',') as int[]),"
			+ "cast(string_to_array('%s',',') as varchar[])," + "cast(string_to_array('%s',',') as varchar[]));";

	public static final QueryConfiguration findTypes() {
		return new QueryConfiguration(FIND_TYPES_TEMPLATE, new Object[0]);
	}

	public static final QueryConfiguration listByType(final ReportType type) {
		final String typeString = type.toString();
		final Object[] arguments = { typeString.toLowerCase() };
		return new QueryConfiguration(LIST_BY_TYPE_TEMPLATE, arguments);
	}

	public static final QueryConfiguration selectById(final int id) {
		return new QueryConfiguration(SELECT_BY_ID_TEMPLATE, new Object[] { id });
	}

	public static final QueryConfiguration delete(final int id) {
		final Object[] arguments = { id };
		return new QueryConfiguration(DELETE_QUERY_TEMPLATE, arguments);
	}

	public static final QueryConfiguration insert(final Report report) {
		final String query = String.format( //
				INSERT_TEMPLATE, //
				arrayToCsv(report.getReportLength()), //
				arrayToCsv(report.getImagesLength()), //
				arrayToCsv(report.getGroups()), //
				arrayToCsv(report.getImagesName()) //
				);

		final Object[] arguments = { "\"" + Report.REPORT_CLASS_NAME + "\"", //
				report.getCode(), //
				report.getDescription(), //
				"A", // active
				report.getUser(), //
				report.getType().toString().toLowerCase(), //
				escape(report.getQuery()), //
				report.getSimpleReport(), //
				report.getRichReportBA(), //
				report.getWizard(), //
				report.getImagesBA() };

		return new QueryConfiguration(query, arguments);
	}

	public static final QueryConfiguration update(final Report report) {

		final List<Object> arguments = new LinkedList<Object>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add(report.getDescription());
				add("A"); // active
				add(report.getUser());
				add(report.getType().toString().toLowerCase());
			}
		};

		final StringBuilder query = new StringBuilder();
		query.append("UPDATE \"Report\" SET \"Description\" = ?, \"Status\" = ?,");
		query.append("\"User\" = ?, \"Type\" = ?, \"Groups\" = cast(string_to_array('");
		query.append(arrayToCsv(report.getGroups()));
		query.append("',',') as varchar[])");

		if (report.getJd() != null) {
			query.append(", \"Query\" = ?, \"SimpleReport\" = ?, \"RichReport\" = ?, \"Wizard\" = ?, \"Images\" = ?, ");
			query.append(String.format("\"ReportLength\" = cast(string_to_array('%s',',') as int[]),",
					arrayToCsv(report.getReportLength())));
			query.append(String.format("\"ImagesLength\" = cast(string_to_array('%s',',') as int[]),",
					arrayToCsv(report.getImagesLength())));
			query.append(String.format("\"ImagesName\" = cast(string_to_array('%s',',') as varchar[]) ",
					arrayToCsv(report.getImagesName())));

			arguments.add(escape(report.getQuery()));
			arguments.add(report.getSimpleReport());
			arguments.add(report.getRichReportBA());
			arguments.add(report.getWizard());
			arguments.add(report.getImagesBA());
		}

		query.append("WHERE \"Id\" = ?;");
		arguments.add(report.getOriginalId());

		return new QueryConfiguration(query.toString(), arguments.toArray());
	}

	private static String escape(final String string) {
		return string.replaceAll("'", "''");
	}

	// + Report.REPORT_CLASS_NAME
	private static String arrayToCsv(final Object array) {
		final StringBuffer output = new StringBuffer();
		if (array != null) {
			for (int i = 0; i < Array.getLength(array); i++) {
				if (i != 0) {
					output.append(",");
				}
				output.append(Array.get(array, i));
			}
		}
		return output.toString();
	}

	public static class QueryConfiguration {
		public final String query;
		public final Object[] arguments;

		public QueryConfiguration(final String query, final Object[] arguments) {
			this.query = query;
			this.arguments = arguments;
		}
	}
}
