package unit.logic.bim;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.logic.bim.project.DefaultProjectLogic;
import org.cmdbuild.logic.bim.project.ProjectLogic;
import org.cmdbuild.logic.bim.project.ProjectLogic.Project;
import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimFacade.BimFacadeProject;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BimLogicProjectCrudTest {

	private static final String EXPORT_ID = "exportId";
	private static final String FILENAME = "ifc";
	private static final String c2 = "22";
	private static final String c1 = "11";
	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private ExportPolicy exportPolicy;
	private ProjectLogic bimLogic;
	private static final String ID = "id of pippo";
	private static final String NAME = "pippo";
	private static final String DESCRIPTION = "description of pippo";
	private static final String IMPORT = "import";
	private static final String EXPORT = "export";
	boolean STATUS = true;

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		exportPolicy = mock(ExportPolicy.class);
		bimLogic = new DefaultProjectLogic(serviceFacade, dataPersistence, exportPolicy);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void createProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> convertedProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getFile()).thenReturn(null);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		when(project.isActive()).thenReturn(STATUS);

		final BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(ID);
		when(createdProject.getName()).thenReturn(NAME);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(createdProject.getExportProjectId()).thenReturn(EXPORT_ID);
		when(serviceFacade.createProjectAndUploadFile(convertedProjectCaptor.capture())).thenReturn(createdProject);
		when(exportPolicy.createProjectForExport(ID)).thenReturn(null);

		// when
		final Project result = bimLogic.createProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		final BimFacadeProject convertedProject = convertedProjectCaptor.getValue();
		inOrder.verify(serviceFacade).createProjectAndUploadFile(convertedProject);
		inOrder.verify(exportPolicy).createProjectForExport(ID);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence, exportPolicy);

		assertTrue(convertedProject.getName().equals(NAME));
		assertTrue(convertedProject.getFile() == null);
		assertTrue(convertedProject.isActive() == STATUS);
		assertTrue(convertedProject.getProjectId() == null);

		final PersistenceProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getExportProjectId() == null);

		assertTrue(result.getName().equals(NAME));
		assertTrue(result.getDescription() == DESCRIPTION);
		assertTrue(result.getLastCheckin() == null);
		assertTrue(result.isActive() == STATUS);
		assertTrue(result.getCardBinding() == null);
		assertTrue(result.getProjectId() == ID);

		result.getFile(); // unsupported
	}

	@Test
	public void projectDisabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		STATUS = true;

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		// when
		bimLogic.disableProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		inOrder.verify(serviceFacade).disableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).disableProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
		verifyZeroInteractions(exportPolicy);
		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.isActive() == STATUS);
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.getName().equals(NAME));

		final PersistenceProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getLastCheckin() == null);
	}

	@Test
	public void projectEnabled() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		STATUS = false;

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getFile()).thenReturn(null);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getDescription()).thenReturn(DESCRIPTION);

		// when
		bimLogic.enableProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		inOrder.verify(serviceFacade).enableProject(facadeProjectCaptor.capture());
		inOrder.verify(dataPersistence).enableProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
		verifyZeroInteractions(exportPolicy);

		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.isActive() == STATUS);
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.getName().equals(NAME));

		final PersistenceProject projectToStore = cmProjectCaptor.getValue();
		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);
		assertTrue(projectToStore.getCardBinding() == null);
		assertTrue(projectToStore.getLastCheckin() == null);
	}

	@Test
	public void readEmptyProjectList() throws Exception {
		// given
		final Iterable<PersistenceProject> projectList = Lists.newArrayList();
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		final Iterable<Project> projects = bimLogic.readAllProjects();

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, exportPolicy);

		assertTrue(Iterables.size(projects) == 0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readProjectListWithOneProject() throws Exception {
		// given
		final List<PersistenceProject> projectList = Lists.newArrayList();
		final PersistenceProject project = mock(PersistenceProject.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getDescription()).thenReturn(DESCRIPTION);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getImportMapping()).thenReturn(IMPORT);
		when(project.getExportMapping()).thenReturn(EXPORT);

		final Iterable<String> cardsBinded = Lists.newArrayList(c1, c2);
		when(project.getCardBinding()).thenReturn(cardsBinded);
		projectList.add(project);
		when(dataPersistence.readAll()).thenReturn(projectList);

		// when
		final Iterable<Project> projects = bimLogic.readAllProjects();

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		inOrder.verify(dataPersistence).readAll();
		verifyNoMoreInteractions(serviceFacade);
		verifyZeroInteractions(serviceFacade, exportPolicy);

		final Project theOnlyProject = projects.iterator().next();
		final Iterable<String> cardBinding = theOnlyProject.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();

		assertTrue(Iterables.size(projects) == 1);
		assertTrue(theOnlyProject.getProjectId().equals(ID));
		assertTrue(theOnlyProject.getName().equals(NAME));
		assertTrue(theOnlyProject.getDescription().equals(DESCRIPTION));
		assertTrue(theOnlyProject.isActive() == STATUS);
		assertTrue(theOnlyProject.getImportMapping().equals(IMPORT));
		assertTrue(theOnlyProject.getExportMapping().equals(EXPORT));
		assertTrue(Iterables.size(cardBinding) == 2);
		assertTrue(iterator.next().equals(c1));
		assertTrue(iterator.next().equals(c2));

		theOnlyProject.getFile(); // unsupported
	}

	@Test
	public void updateProjectWithoutFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getDescription()).thenReturn(DESCRIPTION);
		when(project.isActive()).thenReturn(STATUS);
		when(project.getImportMapping()).thenReturn(IMPORT);
		when(project.getExportMapping()).thenReturn(EXPORT);
		final Iterable<String> cardsBinded = Lists.newArrayList(c1, c2);
		when(project.getCardBinding()).thenReturn(cardsBinded);
		when(project.getFile()).thenReturn(null);

		final BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(ID);
		when(updatedProject.getLastCheckin()).thenReturn(null);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);

		// when
		bimLogic.updateProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		final BimFacadeProject facadeProject = facadeProjectCaptor.getValue();
		inOrder.verify(serviceFacade).updateProject(facadeProject);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence);
		verifyZeroInteractions(exportPolicy);
		final PersistenceProject projectToStore = cmProjectCaptor.getValue();

		assertTrue(facadeProject.getProjectId().equals(ID));
		assertTrue(facadeProject.getName().equals(NAME));
		assertTrue(facadeProject.getFile() == null);
		assertTrue(facadeProject.isActive() == STATUS);

		assertTrue(projectToStore.getProjectId().equals(ID));
		assertTrue(projectToStore.getName().equals(NAME));
		assertTrue(projectToStore.getDescription().equals(DESCRIPTION));
		assertTrue(projectToStore.isActive() == STATUS);

		final Iterable<String> cardBinding = projectToStore.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();
		assertTrue(Iterables.size(cardBinding) == 2);
		assertTrue(iterator.next().equals(c1));
		assertTrue(iterator.next().equals(c2));
	}

	@Test
	public void updateProjectWithFile() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		final File ifcFile = new File(FILENAME);
		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(ID);
		when(project.getName()).thenReturn(NAME);
		when(project.getFile()).thenReturn(ifcFile);

		final BimFacadeProject updatedProject = mock(BimFacadeProject.class);
		when(updatedProject.getProjectId()).thenReturn(ID);
		final DateTime now = new DateTime();
		when(updatedProject.getLastCheckin()).thenReturn(now);
		when(serviceFacade.updateProject(facadeProjectCaptor.capture())).thenReturn(updatedProject);
		final PersistenceProject storedProject = mock(PersistenceProject.class);
		when(storedProject.getExportProjectId()).thenReturn(StringUtils.EMPTY);
		when(storedProject.getShapeProjectId()).thenReturn(StringUtils.EMPTY);
		when(dataPersistence.read(ID)).thenReturn(storedProject);

		// when
		bimLogic.updateProject(project);

		// then
		final BimFacadeProject projectToUpdate = facadeProjectCaptor.getValue();
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		inOrder.verify(serviceFacade).updateProject(projectToUpdate);
		inOrder.verify(exportPolicy).updateProjectForExport(ID);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(serviceFacade, dataPersistence, exportPolicy);

		assertTrue(projectToUpdate.getProjectId().equals(ID));
		assertTrue(projectToUpdate.getName().equals(NAME));
		assertTrue(projectToUpdate.getFile().getName().equals(FILENAME));

		final PersistenceProject projectToSave = cmProjectCaptor.getValue();
		assertTrue(projectToSave.getProjectId().equals(ID));
		assertTrue(projectToSave.getName().equals(NAME));
		assertTrue(projectToSave.getLastCheckin().equals(now));
	}

	@Test
	public void projectCardIsBindedToOneCards() throws Exception {
		// given
		final ArgumentCaptor<BimFacadeProject> facadeProjectCaptor = ArgumentCaptor.forClass(BimFacadeProject.class);
		final ArgumentCaptor<PersistenceProject> cmProjectCaptor = ArgumentCaptor.forClass(PersistenceProject.class);

		final Project project = mock(Project.class);
		when(project.getName()).thenReturn(NAME);
		when(project.getProjectId()).thenReturn(ID);
		final List<String> stringList = Lists.newArrayList();
		stringList.add(c1);
		when(project.getCardBinding()).thenReturn(stringList);

		final BimFacadeProject createdProject = mock(BimFacadeProject.class);
		when(createdProject.getProjectId()).thenReturn(ID);
		when(createdProject.getLastCheckin()).thenReturn(null);
		when(createdProject.getExportProjectId()).thenReturn(EXPORT_ID);
		when(serviceFacade.createProjectAndUploadFile(facadeProjectCaptor.capture())).thenReturn(createdProject);
		when(dataPersistence.findRoot()).thenReturn(new StorableLayer("root"));
		when(exportPolicy.createProjectForExport(ID)).thenReturn(null);
		// when
		bimLogic.createProject(project);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, exportPolicy);
		final BimFacadeProject bimProject = facadeProjectCaptor.getValue();
		inOrder.verify(serviceFacade).createProjectAndUploadFile(bimProject);
		inOrder.verify(exportPolicy).createProjectForExport(ID);
		inOrder.verify(dataPersistence).saveProject(cmProjectCaptor.capture());
		verifyNoMoreInteractions(dataPersistence, serviceFacade, exportPolicy);

		final PersistenceProject projectToSave = cmProjectCaptor.getValue();
		assertTrue(projectToSave.getProjectId().equals(ID));
		assertTrue(projectToSave.getName().equals(NAME));
		assertTrue(projectToSave.getExportProjectId() == null);

		final Iterable<String> cardBinding = projectToSave.getCardBinding();
		final Iterator<String> iterator = cardBinding.iterator();
		assertTrue(Iterables.size(cardBinding) == 1);
		assertTrue(iterator.next().equals(c1));
	}

}
