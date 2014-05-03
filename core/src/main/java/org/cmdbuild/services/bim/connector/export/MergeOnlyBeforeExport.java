package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.INVALID_ID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.services.bim.BimFacade;

public class MergeOnlyBeforeExport implements ExportPolicy {

	private static final String EXPORT_SUFFIX = "-export";
	private final BimFacade bimFacade;
	private final ExportPolicy delegate;

	public MergeOnlyBeforeExport(final BimFacade bimFacade, final ExportPolicy delegate) {
		this.bimFacade = bimFacade;
		this.delegate = delegate;
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
				cloneRevisionToExportProject(revisionId, exportProjectId);
			}
		}
		return exportProjectId;
	}

	private void cloneRevisionToExportProject(final String baseRevisionId, final String exportProjectId) {
		if (!INVALID_ID.equals(exportProjectId)) {
			final Runnable uploadFileOnExportProject = new Runnable() {
				@Override
				public void run() {
					bimFacade.branchRevisionToExistingProject(baseRevisionId, exportProjectId);
				}
			};
			final Thread threadForUpload = new Thread(uploadFileOnExportProject);
			threadForUpload.start();
		}
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
		final String shapeProjectId = getShapeProjectId(exportProjectId);
		if (INVALID_ID.equals(shapeProjectId)) {
			throw new BimError("No shapes loaded");
		}
		final String mergedProject = bimFacade.mergeProjectsIntoNewProject(shapeProjectId, exportProjectId);

		final DataHandler exportedData = bimFacade.download(mergedProject);
		try {
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			exportedData.writeTo(outputStream);
			bimFacade.checkin(exportProjectId, file);
			System.out.println("export file is ready");
		} catch (final IOException e) {
			throw new BimError("Unable to prepare project for export connector");
		}
		System.out.println("Project for export is ready");
	}

	@Override
	public boolean forceUpdate() {
		return delegate.forceUpdate();
	}

}
