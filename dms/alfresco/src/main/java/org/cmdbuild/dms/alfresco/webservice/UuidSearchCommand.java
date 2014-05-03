package org.cmdbuild.dms.alfresco.webservice;

import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class UuidSearchCommand extends AbstractSearchCommand<ResultSetRow> {

	private static final ResultSetRow NULL_RESULT_SET_ROW = null;

	private String uuid;

	public UuidSearchCommand() {
		setResult(NULL_RESULT_SET_ROW);
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(uuid), "invalid category root '%s'", uuid);
		setResult(NULL_RESULT_SET_ROW);
		try {
			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();

			final Reference reference = new Reference();
			reference.setStore(STORE);
			reference.setUuid(uuid);

			final Predicate predicate = new Predicate(new Reference[] { reference }, STORE, null);

			final Node node = repository.get(predicate)[0];
			final ResultSetRow resultSetRow = new ResultSetRow();
			resultSetRow.setColumns(node.getProperties());
			setResult(resultSetRow);
		} catch (final Exception e) {
			logger.error("error while search for uuid", e);
		}
	}

	@Override
	public boolean isSuccessfull() {
		return (getResult() != NULL_RESULT_SET_ROW);
	}

}
