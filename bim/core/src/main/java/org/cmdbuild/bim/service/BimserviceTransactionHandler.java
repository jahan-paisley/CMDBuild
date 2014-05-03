package org.cmdbuild.bim.service;

import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;

import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

public class BimserviceTransactionHandler {

	private final BimService service;
	private final String projectId;
	private final String revisionId;

	private String transactionId = "";

	private static final Logger logger = LoggerSupport.logger;

	protected BimserviceTransactionHandler(BimService service, String projectId) {
		this.service = service;
		this.projectId = projectId;
		revisionId = service.getLastRevisionOfProject(projectId);
	}

	protected String openTransaction() {
		if (transactionId.isEmpty()) {
			transactionId = service.openTransaction(projectId);
			logger.info("Transaction opened on project " + projectId);
			logger.info("Last revision " + revisionId);
		}
		return transactionId;
	}

	protected String commitTransaction() {
		if (!transactionId.isEmpty()) {
			try {
				String newRevisionId = service.commitTransaction(transactionId);
				logger.info(newRevisionId + " revision committed");
				return newRevisionId;
			} catch (Exception e) {
				service.abortTransaction(transactionId);
				logger.info("transaction aborted");
				return INVALID_ID;
			}
		} else {
			logger.info("Transaction has not started yet: cannot commit Transaction");
			return revisionId;
		}
	}

}
