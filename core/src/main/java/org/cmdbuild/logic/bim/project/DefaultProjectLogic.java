package org.cmdbuild.logic.bim.project;

import javax.activation.DataHandler;

import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class DefaultProjectLogic implements ProjectLogic {

	private final BimFacade bimServiceFacade;
	private final BimPersistence bimPersistence;
	private final ExportPolicy exportPolicy;

	public DefaultProjectLogic( //
			final BimFacade bimServiceFacade, //
			final BimPersistence bimPersistence, //
			final ExportPolicy exportStrategy) {

		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.exportPolicy = exportStrategy;
	}

	@Override
	public Iterable<Project> readAllProjects() {
		final Iterable<PersistenceProject> cmProjectList = bimPersistence.readAll();
		final Iterable<Project> projectList = listFrom(cmProjectList);
		return projectList;
	}

	@Override
	public Project createProject(final Project project) {

		final BimFacadeProject bimProject = LOGIC_TO_BIM_PROJECT.apply(project);
		final BimFacadeProject baseProject = bimServiceFacade.createProjectAndUploadFile(bimProject);
		final String projectId = baseProject.getProjectId();
		final String exportProjectId = exportPolicy.createProjectForExport(projectId);

		final PersistenceProject cmProject = LOGIC_TO_PERSISTENCE_PROJECT.apply(project);
		cmProject.setProjectId(projectId);
		cmProject.setLastCheckin(baseProject.getLastCheckin());
		cmProject.setSynch(project.isSynch());
		cmProject.setExportProjectId(exportProjectId);
		bimPersistence.saveProject(cmProject);

		final Project result = PERSISTENCE_TO_LOGIC_PROJECT.apply(cmProject);
		return result;
	}

	@Override
	public void updateProject(final Project project) {
		final String projectId = project.getProjectId();
		final BimFacadeProject bimProject = LOGIC_TO_BIM_PROJECT.apply(project);
		final BimFacadeProject updatedProject = bimServiceFacade.updateProject(bimProject);

		final PersistenceProject persistenceProject = LOGIC_TO_PERSISTENCE_PROJECT.apply(project);
		if (updatedProject.getLastCheckin() != null) {
			persistenceProject.setLastCheckin(updatedProject.getLastCheckin());
		}
		if (project.getFile() != null) {
			final String exportProjectId = exportPolicy.updateProjectForExport(projectId);
			persistenceProject.setExportProjectId(exportProjectId);
		}
		bimPersistence.saveProject(persistenceProject);
	}

	@Override
	public void disableProject(final Project project) {
		final BimFacadeProject bimProject = LOGIC_TO_BIM_PROJECT.apply(project);
		bimServiceFacade.disableProject(bimProject);

		final PersistenceProject persistenceProject = LOGIC_TO_PERSISTENCE_PROJECT.apply(project);
		bimPersistence.disableProject(persistenceProject);
	}

	@Override
	public void enableProject(final Project project) {
		final BimFacadeProject bimProject = LOGIC_TO_BIM_PROJECT.apply(project);
		bimServiceFacade.enableProject(bimProject);

		final PersistenceProject persistenceProject = LOGIC_TO_PERSISTENCE_PROJECT.apply(project);
		bimPersistence.enableProject(persistenceProject);
	}

	@Override
	public DataHandler download(final String projectId) {
		return bimServiceFacade.download(projectId);
	}

	private static final Function<PersistenceProject, Project> PERSISTENCE_TO_LOGIC_PROJECT = new Function<PersistenceProject, Project>() {
		@Override
		public Project apply(final PersistenceProject input) {
			return new ProjectWrapper(input);
		};
	};

	private static final Iterable<Project> listFrom(final Iterable<PersistenceProject> cmProjectList) {
		return Iterables.transform(cmProjectList, PERSISTENCE_TO_LOGIC_PROJECT);
	}

	private static final Function<Project, BimFacadeProject> LOGIC_TO_BIM_PROJECT = new Function<ProjectLogic.Project, BimFacade.BimFacadeProject>() {
		@Override
		public BimFacadeProject apply(final Project input) {
			final DefaultBimFacadeProject bimProject = new DefaultBimFacadeProject();
			bimProject.setName(input.getName());
			bimProject.setFile(input.getFile());
			bimProject.setActive(input.isActive());
			bimProject.setProjectId(input.getProjectId());
			return bimProject;
		}
	};

	private static final Function<Project, PersistenceProject> LOGIC_TO_PERSISTENCE_PROJECT = new Function<Project, PersistenceProject>() {

		@Override
		public PersistenceProject apply(final Project input) {
			final PersistenceProject cmProject = new DefaultPersistenceProject();
			cmProject.setName(input.getName());
			cmProject.setDescription(input.getDescription());
			cmProject.setCardBinding(input.getCardBinding());
			cmProject.setActive(input.isActive());
			cmProject.setProjectId(input.getProjectId());
			return cmProject;
		}
	};

}
