package unit.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.services.store.DashboardStore;
import org.junit.Before;
import org.junit.Test;

public class DashboardLogicTest {
	private static DashboardLogic logic;
	private static DashboardStore store;
	private static OperationUser operationUser;

	@Before
	public void setUp() {
		final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
		when(authUser.getGroupNames()).thenReturn(new HashSet<String>());
		final PrivilegeContext privilegeCtx = mock(PrivilegeContext.class);
		final CMGroup selectedGroup = mock(CMGroup.class);
		operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);
		store = mock(DashboardStore.class);
		logic = new DashboardLogic(null, store, operationUser);
	}

	@SuppressWarnings("serial")
	@Test
	public void addDashboard() {
		final DashboardDefinition dd = new DashboardDefinition();

		when(store.create(dd)).thenReturn(new Long(11));

		final Long newDashboardId = logic.add(dd);

		verify(store).create(dd);
		assertEquals(new Long(11), newDashboardId);

		final DashboardDefinition dd2 = new DashboardDefinition();
		dd2.setColumns(new ArrayList<DashboardColumn>() {
			{
				add(new DashboardColumn());
				add(new DashboardColumn());
			}
		});

		try {
			logic.add(dd2);
			fail("Could not add a dashboard with columns");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardLogic.errors.initDashboardWithColumns();
			assertEquals(expectedMsg, e.getMessage());
		}

		final DashboardDefinition dd3 = new DashboardDefinition();
		dd3.addChart("aChart", new ChartDefinition());

		try {
			logic.add(dd3);
			fail("Could not add a dashboard with charts");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardLogic.errors.initDashboardWithColumns();
			assertEquals(expectedMsg, e.getMessage());
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void modifyDashboard() {
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);
		final Long dashboardId = new Long(123);
		final String name = "name", description = "description";
		final ArrayList<String> groups = new ArrayList<String>() {
			{
				add("a group");
			}
		};

		final DashboardDefinition changes = new DashboardDefinition() {
			{
				setName(name);
				setDescription(description);
				setGroups(groups);
			}
		};

		when(store.read(dashboardId)).thenReturn(dashboard);

		logic.modifyBaseProperties(dashboardId, changes);

		verify(dashboard).setName(name);
		verify(dashboard).setDescription(description);
		verify(dashboard).setGroups(groups);
		verify(store).update(dashboardId, dashboard);

		when(store.read(dashboardId)).thenReturn(null);

		try {
			logic.modifyBaseProperties(dashboardId, changes);
			fail("could not modify a dashboard if it is not stored");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardLogic.errors.undefinedDashboard(dashboardId);
			assertEquals(expectedMsg, e.getMessage());
		}
	}

	@Test
	public void removeDashboard() {
		final Long ddId = new Long(11);

		logic.remove(ddId);

		verify(store).delete(ddId);
	}

	@Test
	public void listDashboard() {
		logic.listDashboards();
		verify(store).list();
	}

	@Test
	public void addChart() {
		final Long dashboardId = new Long(11);
		final ChartDefinition chart = new ChartDefinition();
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.read(dashboardId)).thenReturn(dashboard);

		final String chartId = logic.addChart(dashboardId, chart);

		verify(store).read(dashboardId);
		verify(dashboard).addChart(chartId, chart);
		verify(store).update(dashboardId, dashboard);
	}

	@Test
	public void removeChart() {
		final Long dashboardId = new Long(11);
		final String chartId = "a_unique_id";
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.read(dashboardId)).thenReturn(dashboard);

		logic.removeChart(dashboardId, chartId);

		verify(store).read(dashboardId);
		verify(dashboard).popChart(chartId);
		verify(store).update(dashboardId, dashboard);
	}

	@Test
	public void modifyChart() {
		final Long dashboardId = new Long(11);
		final String chartId = "a_unique_id";
		final ChartDefinition chart = mock(ChartDefinition.class);
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);

		when(store.read(dashboardId)).thenReturn(dashboard);

		logic.modifyChart(dashboardId, chartId, chart);

		verify(store).read(dashboardId);
		verify(dashboard).modifyChart(chartId, chart);
		verify(store).update(dashboardId, dashboard);
	}

	@Test
	public void moveChart() {
		final Long toDashboardId = new Long(11);
		final Long fromDashboardId = new Long(12);
		final String chartId = "a_unique_id";
		final ChartDefinition removedChart = mock(ChartDefinition.class);
		final DashboardDefinition toDashboard = mock(DashboardDefinition.class);
		final DashboardDefinition fromDashboard = mock(DashboardDefinition.class);

		when(store.read(toDashboardId)).thenReturn(toDashboard);
		when(store.read(fromDashboardId)).thenReturn(fromDashboard);
		when(fromDashboard.popChart(chartId)).thenReturn(removedChart);

		logic.moveChart(chartId, fromDashboardId, toDashboardId);

		verify(store).read(fromDashboardId);
		verify(store).read(toDashboardId);

		verify(fromDashboard).popChart(chartId);
		verify(toDashboard).addChart(chartId, removedChart);
		verify(store).update(toDashboardId, toDashboard);
		verify(store).update(fromDashboardId, fromDashboard);
	}

	@Test
	public void setColumns() {
		final Long dashboardId = new Long(12);
		final DashboardDefinition dashboard = mock(DashboardDefinition.class);
		final ArrayList<DashboardColumn> columns = new ArrayList<DashboardColumn>();

		when(store.read(dashboardId)).thenReturn(dashboard);

		logic.setColumns(dashboardId, columns);

		verify(store).read(dashboardId);
		verify(dashboard).setColumns(columns);
		verify(store).update(dashboardId, dashboard);
	}
}
