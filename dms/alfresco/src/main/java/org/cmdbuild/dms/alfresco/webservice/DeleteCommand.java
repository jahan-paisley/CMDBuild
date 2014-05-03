package org.cmdbuild.dms.alfresco.webservice;

import java.util.List;

import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.WebServiceFactory;
import org.cmdbuild.dms.DocumentSearch;

class DeleteCommand extends AbstractSearchCommand<ResultSetRow[]> {

	private static final ResultSetRow[] EMPTY_RESULT_SET_ROWS = new ResultSetRow[0];

	private String baseSearchPath;
	private DocumentSearch target;

	private boolean successfull;

	public DeleteCommand() {
		setResult(EMPTY_RESULT_SET_ROWS);
	}

	public void setBaseSearchPath(final String baseSearchPath) {
		this.baseSearchPath = baseSearchPath;
	}

	public void setTarget(final DocumentSearch target) {
		this.target = target;
	}

	@Override
	public void execute() {
		try {
			final Reference reference = new Reference();
			reference.setStore(STORE);
			reference.setPath(pathOf(target));

			final Predicate predicate = new Predicate(new Reference[] { reference }, STORE, null);

			final CMLDelete delete = new CMLDelete();
			delete.setWhere(predicate);

			final CML cml = new CML();
			cml.setDelete(new CMLDelete[] { delete });

			WebServiceFactory.getRepositoryService().update(cml);

			successfull = true;
		} catch (final Exception e) {
			logger.error("error while deleting file", e);
			successfull = true;
		}
	}

	private String pathOf(final DocumentSearch search) {
		final StringBuilder refstr = new StringBuilder();
		final List<String> path = search.getPath();
		if (path != null) {
			refstr.append(baseSearchPath);
			for (final String p : path) {
				refstr.append("/cm:" + p);
			}
		}
		return refstr.toString();
	}

	@Override
	public boolean isSuccessfull() {
		return successfull;
	}

}
