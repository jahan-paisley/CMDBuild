package unit.logic.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.bim.DefaultLayerLogic;
import org.cmdbuild.logic.bim.LayerLogic;
import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimPersistence;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class BimLogicLayerCrudTest {

	private BimPersistence dataPersistence;
	private BimDataModelManager dataModelManager;
	private BimDataView bimDataView;
	private LayerLogic bimLogic;
	private static final String CLASSNAME = "className";

	private String ATTRIBUTE_NAME;
	private String ATTRIBUTE_VALUE;

	@Before
	public void setUp() throws Exception {
		dataPersistence = mock(BimPersistence.class);
		dataModelManager = mock(BimDataModelManager.class);
		bimDataView = mock(BimDataView.class);
		bimLogic = new DefaultLayerLogic(dataPersistence, bimDataView, dataModelManager);
	}

	@Test
	public void readLayerList() throws Exception {
		// given
		final Iterable<? extends CMClass> classes = new ArrayList<CMClass>();
		when(bimDataView.findClasses()) //
				.thenAnswer(new Answer<Iterable<? extends CMClass>>() {
					@Override
					public Iterable<? extends CMClass> answer(InvocationOnMock invocation) throws Throwable {

						return classes;
					}
				});
		List<StorableLayer> layerList = Lists.newArrayList();
		StorableLayer layer = new StorableLayer(CLASSNAME);
		layerList.add(layer);
		when(dataPersistence.listLayers()).thenReturn(layerList);

		// when
		bimLogic.readLayers();
		//
		// // then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager, bimDataView);

		inOrder.verify(dataPersistence).listLayers();
		inOrder.verify(bimDataView).findClasses();

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerActiveAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "active";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void setContainersetExportLayer() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		ATTRIBUTE_NAME = "export";
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveExportFlag(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerFlag(CLASSNAME, "true");

		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, "true");
		inOrder.verify(dataPersistence).saveContainerFlag(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportFlag(CLASSNAME, "true");

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		when(dataPersistence.findRoot()).thenReturn(null);

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRootFlag(CLASSNAME, true);

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithTrueValueWithNotNullOldBimRoot() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "true";
		String OTHER_CLASS = "anotherClass";
		when(dataPersistence.findRoot()).thenReturn(new StorableLayer(OTHER_CLASS));

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, "true");
		inOrder.verify(dataPersistence).findRoot();
		inOrder.verify(dataModelManager).deleteBimDomainIfExists(OTHER_CLASS);
		inOrder.verify(dataPersistence).saveRootFlag(OTHER_CLASS, false);
		inOrder.verify(dataModelManager).createBimDomainOnClass(CLASSNAME);
		inOrder.verify(dataPersistence).saveRootFlag(CLASSNAME, true);

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerBimRootAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "root";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).deleteBimDomainIfExists(CLASSNAME);
		inOrder.verify(dataPersistence).saveRootFlag(CLASSNAME, false);

		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerExportAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveContainerFlag(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPositionFieldIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveExportFlag(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerExportAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "export";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).saveExportFlag(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerContainerAttributeWithTrueValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "true";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataModelManager).createBimTableIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveActiveFlag(CLASSNAME, ATTRIBUTE_VALUE);
		inOrder.verify(dataPersistence).saveExportFlag(CLASSNAME, "false");
		inOrder.verify(dataModelManager).addPerimeterAndHeightFieldsIfNeeded(CLASSNAME);
		inOrder.verify(dataPersistence).saveContainerFlag(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerContainerAttributeWithFalseValue() throws Exception {
		// given
		ATTRIBUTE_NAME = "container";
		ATTRIBUTE_VALUE = "false";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		InOrder inOrder = inOrder(dataPersistence, dataModelManager);
		inOrder.verify(dataPersistence).saveContainerFlag(CLASSNAME, ATTRIBUTE_VALUE);
		verifyNoMoreInteractions(dataPersistence, dataModelManager);
	}

	@Test
	public void updateLayerUnknownAttribute() throws Exception {
		// given
		String ATTRIBUTE_NAME = "unknown";

		// when
		bimLogic.updateBimLayer(CLASSNAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

		// then
		verifyZeroInteractions(dataPersistence, dataModelManager);
	}

}
