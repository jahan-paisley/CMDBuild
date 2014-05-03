package unit.model.menu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.services.store.menu.MenuItemConverter;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.cmdbuild.services.store.menu.MenuStore.ReportExtension;
import org.junit.Before;
import org.junit.Test;

public class MenuItemConverterTest {

	private MenuItemConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new MenuItemConverter( //
				mock(CMDataView.class), //
				new SystemDataAccessLogicBuilder(//
						mock(CMDataView.class), //
						mock(LookupStore.class), //
						mock(CMDataView.class), //
						mock(CMDataView.class), //
						new OperationUser( //
								mock(AuthenticatedUser.class), //
								mock(PrivilegeContext.class), //
								mock(CMGroup.class) //
						), //
						mock(LockCardManager.class) //
				));
	}

	@Test
	public void testCMClassConvertion() {
		final CMClass aClass = mockClass("FooName", "FooDescription");
		final CMDataView dataView = mockDataView();

		final MenuItem menuItem = converter.fromCMClass(aClass, dataView);
		assertEquals(MenuItemType.CLASS, menuItem.getType());
		assertEquals("FooName", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals("", menuItem.getGroupName());
		assertEquals(new Integer(0), menuItem.getReferencedElementId());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testReportPDFConvertion() {
		final Number id = new Integer(12);
		final CMCard aReport = mockCard(id, "FooDescription");

		final MenuItem menuItem = converter.fromCMReport(aReport, ReportExtension.PDF);

		assertEquals(MenuItemType.REPORT_PDF, menuItem.getType());
		assertEquals("Report", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, menuItem.getReferencedElementId());
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testReportCSVConvertion() {
		final Number id = new Integer(12);
		final CMCard aReport = mockCard(id, "FooDescription");

		final MenuItem menuItem = converter.fromCMReport(aReport, ReportExtension.CSV);

		assertEquals(MenuItemType.REPORT_CSV, menuItem.getType());
		assertEquals("Report", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, menuItem.getReferencedElementId());
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testDashboardConvertion() {
		final Integer id = new Integer(12);

		final MenuItem menuItem = converter.fromDashboard(mockDashboard("FooDescription"), id);

		assertEquals(MenuItemType.DASHBOARD, menuItem.getType());
		assertEquals("_Dashboard", menuItem.getReferedClassName());
		assertEquals("FooDescription", menuItem.getDescription());
		assertEquals(id, menuItem.getReferencedElementId());
		assertEquals("", menuItem.getGroupName());
		assertEquals(0, menuItem.getIndex());
		assertEquals(0, menuItem.getChildren().size());
	}

	private CMClass mockClass(final String name, final String description) {
		final CMClass mockClass = mock(CMClass.class);
		final CMIdentifier mockIdentifier = mock(CMIdentifier.class);
		when(mockIdentifier.getLocalName()).thenReturn(name);
		when(mockClass.getIdentifier()).thenReturn(mockIdentifier);
		when(mockClass.getIdentifier().getLocalName()).thenReturn(name);
		when(mockClass.getDescription()).thenReturn(description);

		return mockClass;
	}

	private CMCard mockCard(final Number id, final String description) {
		final CMCard mockCard = mock(CMCard.class);
		final CMClass mockReport = mockClass("Report", "Report");
		when(mockCard.getDescription()).thenReturn(description);
		when(mockCard.getType()).thenReturn(mockReport);
		when(mockCard.getId()).thenReturn(Long.valueOf(id.toString()));

		return mockCard;
	}

	private DashboardDefinition mockDashboard(final String description) {
		final DashboardDefinition mock = mock(DashboardDefinition.class);
		when(mock.getDescription()).thenReturn(description);

		return mock;
	}

	private CMDataView mockDataView() {
		final CMDataView mockDataView = mock(CMDataView.class);
		when(mockDataView.findClass(Constants.BASE_PROCESS_CLASS_NAME)).thenReturn(null);

		return mockDataView;
	}
}
