package org.cmdbuild.logic;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.contains;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.Category;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.services.store.DashboardStore;

import com.google.common.base.Predicate;

/**
 * Business Logic Layer for Dashboards
 */
public class DashboardLogic implements Logic {

	public static final ErrorMessageBuilder errors = new ErrorMessageBuilder();

	private static final Predicate<CMFunction> EXCLUDE_SYSTEM_FUNCTIONS = new Predicate<CMFunction>() {
		@Override
		public boolean apply(final CMFunction input) {
			return !contains(input.getCategories(), Category.SYSTEM);
		}
	};

	private final CMDataView view;
	private final DashboardStore store;
	private final OperationUser operationUser;

	public DashboardLogic( //
			final CMDataView view, //
			final DashboardStore store, //
			final OperationUser operationUser) {
		this.view = view;
		this.store = store;
		this.operationUser = operationUser;
	}

	public Long add(final DashboardDefinition dashboardDefinition) {
		if (dashboardDefinition.getColumns().size() > 0 || dashboardDefinition.getCharts().size() > 0) {
			throw new IllegalArgumentException(errors.initDashboardWithColumns());
		}

		return store.create(dashboardDefinition);
	}

	public void modifyBaseProperties(final Long dashboardId, final DashboardDefinition changes) {
		final DashboardDefinition dashboard = store.read(dashboardId);

		if (dashboard != null) {
			dashboard.setName(changes.getName());
			dashboard.setDescription(changes.getDescription());
			dashboard.setGroups(changes.getGroups());

			store.update(dashboardId, dashboard);
		} else {
			throw new IllegalArgumentException(errors.undefinedDashboard(dashboardId));
		}
	}

	public void remove(final Long dashboardId) {
		store.delete(dashboardId);
	}

	public Map<Integer, DashboardDefinition> listDashboards() {
		final Map<Integer, DashboardDefinition> dashboards = store.list();
		/*
		 * business rule: the admin can show all dashboards, because is the same
		 * behaviour that is implemented for the reports
		 */
		if (operationUser.hasAdministratorPrivileges()) {
			return dashboards;
		}
		final Map<Integer, DashboardDefinition> allowedDashboards = new HashMap<Integer, DashboardDefinition>();
		final String currentSelectedGroup = operationUser.getPreferredGroup().getName();
		for (final Integer key : dashboards.keySet()) {
			final DashboardDefinition dashboardDefinition = dashboards.get(key);
			if (dashboardDefinition.getGroups().contains(currentSelectedGroup)) {
				allowedDashboards.put(key, dashboardDefinition);
			}
		}
		return allowedDashboards;
	}

	public Map<Integer, DashboardDefinition> fullListDashboards() {
		return store.list();
	}

	public Iterable<? extends CMFunction> listDataSources() {
		return from(view.findAllFunctions()) //
				.filter(EXCLUDE_SYSTEM_FUNCTIONS);
	}

	public GetChartDataResponse getChartData(final String functionName, final Map<String, Object> params) {
		final CMFunction function = view.findFunctionByName(functionName);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = view.select(anyAttribute(function, f)).from(call(function, params), f).run();
		final GetChartDataResponse response = new GetChartDataResponse();
		for (final CMQueryRow row : queryResult) {
			response.addRow(row.getValueSet(f).getValues());
		}
		return response;
	}

	public GetChartDataResponse getChartData(final Long dashboardId, final String chartId,
			final Map<String, Object> params) {

		final DashboardDefinition dashboard = store.read(dashboardId);
		final ChartDefinition chart = dashboard.getChart(chartId);

		return getChartData(chart.getDataSourceName(), params);
	}

	public String addChart(final Long dashboardId, final ChartDefinition chartDefinition) {
		final DashboardDefinition dashboard = store.read(dashboardId);
		final String chartId = UUID.randomUUID().toString();
		dashboard.addChart(chartId, chartDefinition);
		// add the chart to the first column if it has some
		// column configured
		final List<DashboardColumn> columns = dashboard.getColumns();
		if (columns.size() > 0) {
			columns.get(0).addChart(chartId);
		}

		store.update(dashboardId, dashboard);
		return chartId;
	}

	public void removeChart(final Long dashboardId, final String chartId) {
		final DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.popChart(chartId);
		store.update(dashboardId, dashboard);
	}

	public void moveChart(final String chartId, final Long fromDashboardId, final Long toDashboardId) {

		final DashboardDefinition to = store.read(toDashboardId);
		final DashboardDefinition from = store.read(fromDashboardId);
		final ChartDefinition chart = from.popChart(chartId);

		to.addChart(chartId, chart);

		store.update(toDashboardId, to);
		store.update(fromDashboardId, from);
	}

	public void modifyChart(final Long dashboardId, final String chartId, final ChartDefinition chart) {

		final DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.modifyChart(chartId, chart);

		store.update(dashboardId, dashboard);
	}

	public void setColumns(final Long dashboardId, final ArrayList<DashboardColumn> columns) {
		final DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.setColumns(columns);
		store.update(dashboardId, dashboard);
	}

	/*
	 * DTOs
	 */

	public static class GetChartDataResponse {
		private final List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		public List<Map<String, Object>> getRows() {
			return rows;
		}

		private void addRow(final Iterable<Entry<String, Object>> row) {
			final Map<String, Object> dataRow = new HashMap<String, Object>();
			for (final Entry<String, Object> entry : row) {
				dataRow.put(entry.getKey(), entry.getValue());
			}

			rows.add(dataRow);
		}
	}

	/*
	 * to avoid an useless errors hierarchy define this object that build the
	 * errors messages These are used also in the tests to ensure that a right
	 * message is provided by the exception
	 */
	public static class ErrorMessageBuilder {
		public String initDashboardWithColumns() {
			return "Cannot add a Dashbaord if it has already columns or charts";
		}

		public String undefinedDashboard(final Long dashboardId) {
			final String errorFormat = "There is no dashboard with id %d";
			return String.format(errorFormat, dashboardId);
		}
	}
}
