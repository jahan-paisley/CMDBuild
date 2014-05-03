package unit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.junit.Before;
import org.junit.Test;

public class DashboardDefinitionTest {
	private static DashboardDefinition dashboard;

	@Before
	public void setUp() {
		dashboard = new DashboardDefinition();
		dashboard.setName("Foo");
	}

	@Test
	public void addChartToDashboard() {
		final ChartDefinition chart = new ChartDefinition();
		final String chartId = "theId";

		assertEquals(0, dashboard.getCharts().size());
		dashboard.addChart(chartId, chart);
		assertEquals(1, dashboard.getCharts().size());

		try {
			dashboard.addChart(chartId, chart);
			fail("Cannot use the same Id twice");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.duplicateChartIdForDashboard(chartId,
					dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}
	}

	@Test
	public void getChart() {
		final ChartDefinition chart = new ChartDefinition();
		final String chartId = "theId";

		try {
			dashboard.getChart(chartId);
			fail("Cannot retrieve a chart from an empty dashboard");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.notFoundChartIdForDashboard(chartId,
					dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}

		dashboard.addChart(chartId, chart);

		assertEquals(chart, dashboard.getChart(chartId));
	}

	@Test
	public void modifyChartToDashboard() {
		final String chartId = "theId";

		final ChartDefinition chart = new ChartDefinition();
		final ChartDefinition secondChart = new ChartDefinition();

		try {
			dashboard.modifyChart(chartId, null);
			fail("Can not modify a chart that is not in the dashboard");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.notFoundChartIdForDashboard(chartId,
					dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}

		dashboard.addChart(chartId, chart);

		try {
			dashboard.modifyChart(chartId, null);
			fail("Can not modify a chart that is not in the dashboard");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.nullChart(dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}

		dashboard.modifyChart(chartId, secondChart);
		assertEquals(secondChart, dashboard.getChart(chartId));
	}

	@Test
	public void removeChart() {
		final String chartId = "theId";
		final ChartDefinition chart = new ChartDefinition();

		try {
			dashboard.popChart(chartId);
			fail("Cannot remove a chart if you have not add it to the dashboard");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.notFoundChartIdForDashboard(chartId,
					dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}

		dashboard.addChart(chartId, chart);
		final ChartDefinition poppedChart = dashboard.popChart(chartId);

		assertEquals(chart, poppedChart);
	}

	@Test
	public void addColumn() {
		final DashboardDefinition dashboard = new DashboardDefinition();
		final DashboardColumn column = new DashboardColumn();
		final String chartId = "aChart";
		final ChartDefinition chart = new ChartDefinition();

		dashboard.setName("The Dashboard");
		column.addChart(chartId);

		try {
			dashboard.addColumn(column);
			fail("Cannot add a column with a reference to a chart that is not in the dashboard");
		} catch (final IllegalArgumentException e) {
			final String expectedMsg = DashboardDefinition.errors.wrongChartInColumn(chartId, dashboard.getName());
			assertEquals(expectedMsg, e.getMessage());
		}

		dashboard.addChart(chartId, chart);
		dashboard.addColumn(column);

		assertEquals(1, dashboard.getColumns().size());
	}

	@Test
	public void setColumns() {
		final DashboardDefinition dashboard = new DashboardDefinition();
		final DashboardColumn column1 = new DashboardColumn();
		final DashboardColumn column2 = new DashboardColumn();
		final ArrayList<DashboardColumn> columns = new ArrayList<DashboardColumn>();
		final String chart1Id = "aChart";
		final String chart2Id = "aSecondChart";

		column1.addChart(chart1Id);
		column2.addChart(chart2Id);
		columns.add(column1);
		columns.add(column2);

		/*
		 * set the columns also without check on the charts in the dashboard to
		 * allow the json decoding
		 */
		dashboard.setColumns(columns);

		assertEquals(2, dashboard.getColumns().size());
	}

	@Test
	public void removeColumn() {
		final DashboardDefinition dashboard = new DashboardDefinition();
		final DashboardColumn column1 = new DashboardColumn();
		final DashboardColumn column2 = new DashboardColumn();
		final ArrayList<DashboardColumn> columns = new ArrayList<DashboardColumn>();
		final String chart1Id = "aChart";
		final String chart2Id = "aSecondChart";

		column1.addChart(chart1Id);
		column2.addChart(chart2Id);
		columns.add(column1);
		columns.add(column2);

		dashboard.setColumns(columns);

		assertEquals(2, dashboard.getColumns().size());

		dashboard.removeColumn(column2);
		assertEquals(1, dashboard.getColumns().size());
	}
}
