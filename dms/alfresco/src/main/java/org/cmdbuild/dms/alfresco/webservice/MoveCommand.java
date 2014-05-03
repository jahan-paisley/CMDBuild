package org.cmdbuild.dms.alfresco.webservice;

import java.util.List;

import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLMove;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.cmdbuild.dms.DocumentSearch;

class MoveCommand extends AbstractSearchCommand<ResultSetRow[]> {

	private static final ResultSetRow[] EMPTY_RESULT_SET_ROWS = new ResultSetRow[0];

	private String baseSearchPath;
	private String uuid;
	private DocumentSearch target;

	private boolean successfull;

	public MoveCommand() {
		setResult(EMPTY_RESULT_SET_ROWS);
	}

	public void setBaseSearchPath(final String baseSearchPath) {
		this.baseSearchPath = baseSearchPath;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public void setTarget(final DocumentSearch target) {
		this.target = target;
	}

	@Override
	public void execute() {
		try {
			final Reference sourceReference = new Reference(STORE, uuid, null);

			final Predicate sourcePredicate = new Predicate(new Reference[] { sourceReference }, STORE, null);

			final ParentReference targetReference = new ParentReference();
			targetReference.setStore(STORE);
			targetReference.setPath(pathOf(target));
			targetReference.setAssociationType(Constants.ASSOC_CONTAINS);
			targetReference.setChildName(uuid);

			final CMLMove move = new CMLMove();
			move.setWhere(sourcePredicate);
			move.setTo(targetReference);

			final CML cml = new CML();
			cml.setMove(new CMLMove[] { move });

			WebServiceFactory.getRepositoryService().update(cml);

			successfull = true;
		} catch (final Exception e) {
			logger.error("error while moving file", e);
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
