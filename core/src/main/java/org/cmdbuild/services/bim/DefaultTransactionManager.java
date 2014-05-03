package org.cmdbuild.services.bim;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.bim.service.BimService;

public class DefaultTransactionManager implements TransactionManager {

	private final BimService service;
	private long transactionId;
	private static final long NULL_ID = -1;

	public DefaultTransactionManager(final BimService service) {
		this.service = service;
		this.transactionId = -1;
	}
	
	private void reset(){
		this.transactionId = NULL_ID;
	}

	@Override
	public void open(String projectId) {
		if (!hasTransaction()) {
			transactionId = Long.parseLong(service.openTransaction(projectId));
		}
	}

	@Override
	public boolean hasTransaction() {
		return transactionId != -1;
	}

	@Override
	public String getId() {
		return String.valueOf(transactionId);
	}

	@Override
	public String commit() {
		String revisionId = StringUtils.EMPTY;
		if (!hasTransaction()) {
			throw new IllegalStateException("Unable to perform the commit of transaction " + getId());
		}
		try {
			revisionId = service.commitTransaction(getId());
		} catch (Throwable t) {
			abort();
			throw new RuntimeException("Unable to perform the commit", t);
		}
		finally{
			reset();
		}
		return revisionId;
	}

	@Override
	public void abort() {
		service.abortTransaction(getId());
		reset();
	}
}
