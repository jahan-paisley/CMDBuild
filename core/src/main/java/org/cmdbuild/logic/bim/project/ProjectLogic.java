package org.cmdbuild.logic.bim.project;

import java.io.File;

import javax.activation.DataHandler;

import org.cmdbuild.logic.Logic;
import org.joda.time.DateTime;

public interface ProjectLogic extends Logic {

	public static interface Project {

		String getProjectId();

		String getName();

		String getDescription();

		boolean isActive();

		boolean isSynch();

		String getImportMapping();

		String getExportMapping();

		DateTime getLastCheckin();

		Iterable<String> getCardBinding();

		File getFile();

	}

	Iterable<Project> readAllProjects();

	Project createProject(Project project);

	void updateProject(Project project);

	DataHandler download(String projectId);

	void enableProject(Project project);

	void disableProject(Project project);

}