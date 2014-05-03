package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.services.bim.BimFacade;

public class MergeAfterEveryUpload implements ExportPolicy {

	private static final String EXPORT_SUFFIX = "-export";
	private final BimFacade bimFacade;

	public MergeAfterEveryUpload(final BimFacade bimFacade) {
		this.bimFacade = bimFacade;
	}

	@Override
	public String createProjectForExport(final String projectId) {
		String exportProjectId = StringUtils.EMPTY;
		final String revisionId = bimFacade.getLastRevisionOfProject(projectId);
		if (INVALID_ID.equals(revisionId)) {
			exportProjectId = null;
		} else {
			final String projectName = exportProjectName(projectId, revisionId);
			final BimProject exportProjectForRevision = bimFacade.getProjectByName(projectName);
			if (exportProjectForRevision.isValid()) {
				exportProjectId = exportProjectForRevision.getIdentifier();
			} else {
				exportProjectId = bimFacade.createProject(projectName);
				cloneRevisionAndMergeWithShapes(projectId, exportProjectId);
			}
		}
		return exportProjectId;
	}

	private void cloneRevisionAndMergeWithShapes(final String projectId, final String exportProjectId) {
		final String shapeProjectId = getShapeProjectId(projectId);
		final Runnable uploadFileOnExportProject = new Runnable() {

			@Override
			public void run() {
				if (!INVALID_ID.equals(shapeProjectId)) {
					bimFacade.updateExportProject(projectId, exportProjectId, shapeProjectId);
				}
			}
		};
		final Thread threadForUpload = new Thread(uploadFileOnExportProject);
		threadForUpload.start();
	}

	@Override
	public String updateProjectForExport(final String projectId) {
		return createProjectForExport(projectId);

	}

	private static String exportProjectName(final String projectId, final String revisionId) {
		final StringBuilder nameBuilder = new StringBuilder(revisionId);
		nameBuilder.append(EXPORT_SUFFIX);
		return nameBuilder.toString();
	}

	private String getShapeProjectId(final String projectId) {
		final String shapeProjectName = "_shapes";
		final BimProject projectByName = bimFacade.getProjectByName(shapeProjectName);
		return projectByName.getIdentifier();
	}

	@Override
	public void beforeExport(final String exportProjectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean forceUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

}
