package org.cmdbuild.dms.alfresco.webservice;

import java.util.List;

import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.SingleDocumentSearch;

class SingleSearchCommand extends AbstractSearchCommand<ResultSetRow> {

	private static final ResultSetRow NULL_RESULT_SET_ROW = null;

	private SingleDocumentSearch search;
	private String baseSearchPath;

	public SingleSearchCommand() {
		setResult(NULL_RESULT_SET_ROW);
	}

	public void setDocumentSearch(final SingleDocumentSearch search) {
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
			final StringBuilder refstr = new StringBuilder(baseSearchPath);
			final List<String> path = search.getPath();
			for (final String p : path) {
				refstr.append("/cm:").append(p);
			}

			final Reference reference = new Reference(STORE, null, refstr.toString());

			final Predicate predicate = new Predicate(new Reference[] { reference }, null, null);

			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();
			final Node[] nodes = repository.get(predicate);

			final Query query = new Query();
			query.setLanguage(Constants.QUERY_LANG_LUCENE);
			query.setStatement(statement(nodes[0]));

			final QueryResult queryResult = repository.query(STORE, query, false);

			final ResultSet resultSet = queryResult.getResultSet();
			final ResultSetRow[] resultSetRows = resultSet.getRows();
			for (final ResultSetRow resultSetRow : resultSetRows) {
				final NamedValue[] namedValues = resultSetRow.getColumns();
				for (final NamedValue namedValue : namedValues) {
					if (AlfrescoConstant.NAME.isName(namedValue.getName())) {
						if (namedValue.getValue().equals(search.getFileName())) {
							setResult(resultSetRow);
							return;
						}
					}
				}
			}
			setResult(NULL_RESULT_SET_ROW);
		} catch (final Exception e) {
			logger.error("error while search for file", e);
			setResult(NULL_RESULT_SET_ROW);
		}

	}

	@Override
	public boolean isSuccessfull() {
		return (getResult() != NULL_RESULT_SET_ROW);
	}

	private static String statement(final Node node) {
		return String.format("+PARENT:\"%s://%s/%s\" ", //
				Constants.WORKSPACE_STORE, //
				DEFAULT_STORE_ADDRESS, //
				node.getReference().getUuid());
	}

}
