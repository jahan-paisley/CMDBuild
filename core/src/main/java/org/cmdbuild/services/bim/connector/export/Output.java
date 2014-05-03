package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Entity;

public interface Output {
	
	public void outputInvalid(final String outputId);

	public void createTarget(final Entity entityToCreate, final String targetProjectId);

	public void deleteTarget(final Entity entityToRemove, final String targetProjectId);

	public void finalActions(final String targetProjectId);

	public void notifyError(Throwable t);
}
