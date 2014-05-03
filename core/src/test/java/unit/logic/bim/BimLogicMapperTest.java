package unit.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.cmdbuild.logic.bim.DefaultSynchronizationLogic;
import org.cmdbuild.logic.bim.SynchronizationLogic;
import org.cmdbuild.logic.bim.project.ProjectLogic.Project;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.services.bim.connector.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class BimLogicMapperTest {

	private BimFacade serviceFacade;
	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private Mapper mapper;
	private SynchronizationLogic bimLogic;
	private static final String PROJECTID = "123";

	private static String XML_MAPPING = "";

	@Before
	public void setUp() throws Exception {
		serviceFacade = mock(BimFacade.class);
		dataPersistence = mock(BimPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		mapper = mock(Mapper.class);
		bimLogic = new DefaultSynchronizationLogic(serviceFacade, dataPersistence, mapper, null, null);
	}

	@Test
	public void ifXmlMappingIsEmptyDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf></bim-conf>";

		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		final PersistenceProject cmProject = mock(PersistenceProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(dataPersistence).saveProject(any(PersistenceProject.class));
		verifyNoMoreInteractions(dataPersistence);
		verifyZeroInteractions(serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void readOneEntityAndCallTheUpdateOnCMDBOnce() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";
		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		final PersistenceProject cmProject = mock(PersistenceProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		final List<Entity> bimEntityList = Lists.newArrayList();
		final Entity entity = mock(Entity.class);
		final SimpleAttribute globalIdAttribute = mock(SimpleAttribute.class);
		when(globalIdAttribute.isValid()).thenReturn(true);
		when(globalIdAttribute.getStringValue()).thenReturn("guid");
		when(entity.getAttributeByName(GLOBALID_ATTRIBUTE)).thenReturn(globalIdAttribute);
		bimEntityList.add(entity);
		when(serviceFacade.readEntityFromProject(any(EntityDefinition.class), any(String.class))).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(serviceFacade).readEntityFromProject(any(EntityDefinition.class), any(String.class));
		inOrder.verify(mapper).update(bimEntityList);
		inOrder.verify(dataPersistence).saveProject(any(PersistenceProject.class));
		verifyNoMoreInteractions(dataPersistence, serviceFacade, dataModelManager, mapper);
	}

	@Test
	public void fetchNoEntitiesFromBimAndDoNothing() throws Exception {
		// given
		XML_MAPPING = "<bim-conf><entity></entity></bim-conf>";
		final Project project = mock(Project.class);
		when(project.getProjectId()).thenReturn(PROJECTID);
		when(project.getImportMapping()).thenReturn(XML_MAPPING);
		final PersistenceProject cmProject = mock(PersistenceProject.class);
		when(cmProject.getImportMapping()).thenReturn(XML_MAPPING);
		when(dataPersistence.read(PROJECTID)).thenReturn(cmProject);

		final List<Entity> bimEntityList = Lists.newArrayList();
		when(serviceFacade.readEntityFromProject(any(EntityDefinition.class), any(String.class))).thenReturn(
				bimEntityList);

		// when
		bimLogic.importIfc(PROJECTID);

		// then
		final InOrder inOrder = inOrder(serviceFacade, dataPersistence, dataModelManager, mapper);
		inOrder.verify(dataPersistence).read(PROJECTID);
		inOrder.verify(serviceFacade).readEntityFromProject(any(EntityDefinition.class), any(String.class));
		inOrder.verify(dataPersistence).saveProject(any(PersistenceProject.class));
		verifyNoMoreInteractions(dataPersistence, serviceFacade, dataModelManager, mapper);
	}

}
