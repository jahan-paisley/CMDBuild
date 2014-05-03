package org.cmdbuild.services.bim.connector.export;

public class DoNotForceUpdate implements ExportPolicy {

	private final boolean FORCE_UPDATE_YES = false;

	@Override
	public boolean forceUpdate() {
		return FORCE_UPDATE_YES;
	}

	@Override
	public String createProjectForExport(final String projectId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String updateProjectForExport(final String projectId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void beforeExport(final String exportProjectId) {
		throw new UnsupportedOperationException();
	}

}
