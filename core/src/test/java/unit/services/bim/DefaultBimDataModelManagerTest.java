package unit.services.bim;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultBimDataModelManagerTest {

	private static final String THE_CLASS = "Edificio";
	private BimDataModelManager dataModelManager;

	private CMDataView dataView;
	private DataDefinitionLogic dataDefinitionLogic;
	private LookupLogic lookupLogic;
	private DataSource dataSource;

	@Before
	public void setUp() throws Exception {
		dataDefinitionLogic = mock(DataDefinitionLogic.class);
		dataView = mock(CMDataView.class);
		lookupLogic = mock(LookupLogic.class);
		dataSource = mock(DataSource.class);

		dataModelManager = new DefaultBimDataModelManager(dataView, dataDefinitionLogic, lookupLogic, dataSource);
	}

	@Test
	public void bimTableCreated() throws Exception {
		// given
		final ArgumentCaptor<CMIdentifier> identifierCaptor = ArgumentCaptor.forClass(CMIdentifier.class);
		final ArgumentCaptor<EntryType> entryTypeCaptor = ArgumentCaptor.forClass(EntryType.class);

		when(dataView.findClass(identifierCaptor.capture())).thenReturn(null);
		CMClass theClass = mock(CMClass.class);
		when(dataDefinitionLogic.createOrUpdate(entryTypeCaptor.capture())).thenReturn(theClass);

		// when
		dataModelManager.createBimTableIfNeeded(THE_CLASS);

		// then
		InOrder inOrder = inOrder(dataDefinitionLogic, dataView);

		inOrder.verify(dataView).findClass(any(CMIdentifier.class));
		assertThat(identifierCaptor.getValue().getLocalName(), equalTo(THE_CLASS));

		inOrder.verify(dataDefinitionLogic).createOrUpdate(any(EntryType.class));
		assertThat(entryTypeCaptor.getValue().getName(), equalTo(THE_CLASS));

		inOrder.verify(dataDefinitionLogic, times(2)).createOrUpdate(any(Attribute.class));

		verifyNoMoreInteractions(dataDefinitionLogic, dataView);
	}

	@Test
	public void bimTableNotCreated() throws Exception {
		// given
		final ArgumentCaptor<CMIdentifier> identifierCaptor = ArgumentCaptor.forClass(CMIdentifier.class);

		DBClass dbclass = mock(DBClass.class);
		when(dataView.findClass(identifierCaptor.capture())).thenReturn(dbclass);

		// when
		dataModelManager.createBimTableIfNeeded(THE_CLASS);

		// then
		InOrder inOrder = inOrder(dataDefinitionLogic, dataView);
		inOrder.verify(dataView).findClass(any(CMIdentifier.class));
		assertThat(identifierCaptor.getValue().getLocalName(), equalTo(THE_CLASS));

		verifyNoMoreInteractions(dataDefinitionLogic, dataView);
	}

	@Test
	public void bimDomainCreated() throws Exception {
		// given
		ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);
		CMDomain binDomain = mock(CMDomain.class);
		when(dataDefinitionLogic.create(domainCaptor.capture())).thenReturn(binDomain);

		DBClass theClass = mock(DBClass.class);
		DBClass projectClass = mock(DBClass.class);
		when(dataView.findClass(THE_CLASS)).thenReturn(theClass);
		when(dataView.findClass(StorableProjectConverter.TABLE_NAME)).thenReturn(projectClass);
		when(theClass.getId()).thenReturn(new Long(111));
		when(projectClass.getId()).thenReturn(new Long(222));

		// when
		dataModelManager.createBimDomainOnClass(THE_CLASS);

		// then
		InOrder inOrder = inOrder(dataDefinitionLogic, dataView);
		inOrder.verify(dataView).findClass(THE_CLASS);
		inOrder.verify(dataView).findClass(StorableProjectConverter.TABLE_NAME);
		inOrder.verify(dataDefinitionLogic).create(any(Domain.class));
		assertTrue(domainCaptor.getValue().getIdClass1() == 111);
		assertTrue(domainCaptor.getValue().getIdClass2() == 222);

		verifyNoMoreInteractions(dataDefinitionLogic, dataView);
	}

	@Test
	public void bimDomainDeleted() throws Exception {
		// given

		// when
		dataModelManager.deleteBimDomainIfExists(THE_CLASS);

		// then
		InOrder inOrder = inOrder(dataDefinitionLogic, dataView);
		inOrder.verify(dataDefinitionLogic).deleteDomainIfExists(
				THE_CLASS + DefaultBimDataModelManager.DEFAULT_DOMAIN_SUFFIX);

		verifyNoMoreInteractions(dataDefinitionLogic, dataView);
	}

}
