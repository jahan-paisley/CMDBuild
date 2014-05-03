package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Entity;

public class DataChangedListener implements Output {

	@SuppressWarnings("serial")
	public static class DataChangedException extends RuntimeException {
	}

	@SuppressWarnings("serial")
	public static class DataNotChangedException extends RuntimeException {
	}

	@Override
	public void createTarget(final Entity entityToCreate, final String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void deleteTarget(final Entity entityToRemove, final String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void finalActions(final String targetProjectId) {
		throw new DataNotChangedException();
	}

	@Override
	public void outputInvalid(final String outputId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifyError(final Throwable t) {
		if (t instanceof DataChangedException) {
			throw new DataChangedException();
		}
	}

}