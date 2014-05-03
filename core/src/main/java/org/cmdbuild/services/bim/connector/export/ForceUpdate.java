package org.cmdbuild.services.bim.connector.export;

public class ForceUpdate implements ExportPolicy {

	private final boolean FORCE_UPDATE_YES = true;

	@Override
	public boolean forceUpdate() {
		return FORCE_UPDATE_YES;
	}

	@Override
	public String createProjectForExport(String projectId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String updateProjectForExport(String projectId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void beforeExport(String exportProjectId) {
		throw new UnsupportedOperationException();
	}

}
