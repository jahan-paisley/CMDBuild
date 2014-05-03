package org.cmdbuild.dms.alfresco.webservice;

import java.util.List;

import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DocumentSearch;

class SearchCommand extends AbstractSearchCommand<ResultSetRow[]> {

	private static final ResultSetRow[] EMPTY_RESULT_SET_ROWS = new ResultSetRow[0];

	private DocumentSearch search;
	private String baseSearchPath;

	public SearchCommand() {
		setResult(EMPTY_RESULT_SET_ROWS);
	}

	public void setDocumentSearch(final DocumentSearch search) {
		this.search = search;
	}

	public void setBaseSearchPath(final String baseSearchPath) {
		this.baseSearchPath = baseSearchPath;
	}

	@Override
	public void execute() {
		Validate.notNull(search, "null search");
		Validate.notNull(baseSearchPath, "null base search path");

		try {
			final Query query = new Query();
			query.setLanguage(Constants.QUERY_LANG_LUCENE);

			final String statement = statement(search, baseSearchPath);
			query.setStatement(statement);

			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();
			final QueryResult result = repository.query(STORE, query, false);

			final ResultSet resultSet = result.getResultSet();
			final ResultSetRow[] resultSetRows = resultSet.getRows();
			setResult(resultSetRows == null ? EMPTY_RESULT_SET_ROWS : resultSetRows);
		} catch (final Exception e) {
			logger.error("error while search for files", e);
			setResult(EMPTY_RESULT_SET_ROWS);
		}
	}

	@Override
	public boolean isSuccessfull() {
		return true;
	}

	private static String statement(final DocumentSearch search, final String baseSearchPath) {
		final StringBuilder statement = new StringBuilder();
		final StringBuilder refstr = new StringBuilder();
		final List<String> path = search.getPath();
		if (path != null) {
			refstr.append(baseSearchPath);
			for (final String p : path) {
				refstr.append("/cm:" + p);
			}
			statement.append("+PATH:\"" + refstr + "//*\" ");
		}
		return statement.toString();
	}

}
