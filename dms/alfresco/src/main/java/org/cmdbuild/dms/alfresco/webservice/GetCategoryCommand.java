package org.cmdbuild.dms.alfresco.webservice;

import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class GetCategoryCommand extends AbstractSearchCommand<Reference> {

	private static final Reference NULL_REFERENCE = null;

	private String category;

	public GetCategoryCommand() {
		setResult(NULL_REFERENCE);
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(category), "invalid category '%s'", category);
		try {
			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();
			final QueryResult queryResult = repository.query(STORE, queryFor(category), true);
			final ResultSetRow resultSetRow = queryResult.getResultSet().getRows(0);
			final NamedValue[] namedValues = resultSetRow.getColumns();
			Reference out = null;
			for (final NamedValue namedValue : namedValues) {
				if (AlfrescoConstant.UUID.isName(namedValue.getName())) {
					out = new Reference();
					out.setUuid(namedValue.getValue());
					out.setStore(STORE);
					setResult(out);
				}
			}
		} catch (final Exception e) {
			logger.error("error while searching for category", e);
			setResult(NULL_REFERENCE);
		}
	}

	private static Query queryFor(final String category) {
		return new Query(Constants.QUERY_LANG_LUCENE, "PATH:\"/cm:generalclassifiable//cm:" + escapeQuery(category)
				+ "\"");
	}

	@Override
	public boolean isSuccessfull() {
		return (getResult() != NULL_REFERENCE);
	}

}
