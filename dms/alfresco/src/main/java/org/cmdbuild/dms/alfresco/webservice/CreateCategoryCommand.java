package org.cmdbuild.dms.alfresco.webservice;

import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class CreateCategoryCommand extends AlfrescoWebserviceCommand<Boolean> {

	private String categoryRoot;
	private String category;

	public CreateCategoryCommand() {
		setResult(false);
	}

	public void setCategoryRoot(final String categoryRoot) {
		this.categoryRoot = categoryRoot;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(categoryRoot), "invalid category root '%s'", categoryRoot);
		Validate.isTrue(StringUtils.isNotBlank(category), "invalid category '%s'", category);
		try {
			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();

			final Query query = query(categoryRoot);
			final QueryResult queryResult = repository.query(STORE, query, true);
			final ResultSet resultSet = queryResult.getResultSet();
			final ResultSetRow[] resultSetRow = resultSet.getRows();
			final String categoryRootReferenceUuid = resultSetRow[0].getNode().getId();

			final String SUBCATEGORIES = "subcategories";
			final String CATEGORY = "category";
			final String path = category;

			final ParentReference parentReference = new ParentReference( //
					STORE, //
					categoryRootReferenceUuid, //
					null, //
					Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, SUBCATEGORIES), //
					Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, path));

			final NamedValue[] properties = new NamedValue[] { Utils.createNamedValue(Constants.PROP_NAME, path) };

			final CMLCreate create = new CMLCreate("1", //
					parentReference, //
					null, //
					null, //
					null, //
					Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, CATEGORY), //
					properties);
			final CML cml = new CML();
			cml.setCreate(new CMLCreate[] { create });

			WebServiceFactory.getRepositoryService().update(cml);
			setResult(true);
		} catch (final Exception e) {
			final String message = String.format("error creating category '%s'", category);
			logger.info(message, e);
			setResult(false);
		}
	}

	private static Query query(final String categoryRoot) {
		return new Query(Constants.QUERY_LANG_LUCENE, "PATH:\"/cm:generalclassifiable//cm:" + escapeQuery(categoryRoot)
				+ "\"");
	}

	@Override
	public boolean isSuccessfull() {
		return getResult();
	}

}
