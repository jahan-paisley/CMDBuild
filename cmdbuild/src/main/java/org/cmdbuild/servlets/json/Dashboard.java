package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.DashboardLogic.GetChartDataResponse;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.model.dashboard.DashboardObjectMapper;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonDashboardDTO.JsonDashboardListResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class Dashboard extends JSONBaseWithSpringContext {

	private static final ObjectMapper mapper = new DashboardObjectMapper();

	/**
	 * Retrieves all the dashboards configured and all the function data sources
	 * available on the data base
	 */
	@JSONExported
	public JsonResponse fullList() {
		final DashboardLogic logic = dashboardLogic();
		final JsonDashboardListResponse response = new JsonDashboardListResponse(logic.fullListDashboards(),
				logic.listDataSources());
		return JsonResponse.success(response);
	}

	/**
	 * Retrieve all the dashboards configured
	 */
	@JSONExported
	public JsonResponse list() {
		final DashboardLogic logic = dashboardLogic();
		final JsonDashboardListResponse response = new JsonDashboardListResponse(logic.listDashboards());
		return JsonResponse.success(response);
	}

	@Admin
	@JSONExported
	public JsonResponse add( //
			@Parameter(value = "dashboardConfiguration") final String jsonDashboard //
	) throws Exception {
		final DashboardLogic logic = dashboardLogic();
		final DashboardDefinition dashboard = mapper.readValue(jsonDashboard, DashboardDefinition.class);

		final Long dashboardId = logic.add(dashboard);

		return JsonResponse.success(dashboardId);
	}

	@Admin
	@JSONExported
	public void modifyBaseProperties( //
			@Parameter(value = "dashboardId") final Long dashboardId, //
			@Parameter(value = "dashboardConfiguration") final String jsonDashboard //
	) throws Exception {

		final DashboardLogic logic = dashboardLogic();
		final DashboardDefinition dashboard = mapper.readValue(jsonDashboard, DashboardDefinition.class);

		logic.modifyBaseProperties(dashboardId, dashboard);
	}

	@Admin
	@JSONExported
	public void modifyColumns(//
			@Parameter(value = "dashboardId") final Long dashboardId, //
			@Parameter(value = "columnsConfiguration") final String jsonColumns //
	) throws Exception {

		final DashboardLogic logic = dashboardLogic();
		final ArrayList<DashboardColumn> columns = mapper.readValue(jsonColumns,
				new TypeReference<ArrayList<DashboardColumn>>() {
				});

		logic.setColumns(dashboardId, columns);
	}

	@Admin
	@JSONExported
	public void remove( //
			@Parameter(value = "dashboardId") final Long dashboardId) {

		final DashboardLogic logic = dashboardLogic();

		logic.remove(dashboardId);
	}

	@Admin
	@JSONExported
	public JsonResponse addChart( //
			@Parameter(value = "dashboardId") final Long dashboardId, //
			@Parameter(value = "chartConfiguration") final String jsonChartConfiguration //
	) throws Exception {

		final DashboardLogic logic = dashboardLogic();
		final ChartDefinition chartDefinition = mapper.readValue(jsonChartConfiguration, ChartDefinition.class);

		return JsonResponse.success(logic.addChart(dashboardId, chartDefinition));
	}

	@Admin
	@JSONExported
	public void modifyChart(@Parameter(value = "dashboardId") final Long dashboardId, //
			@Parameter(value = "chartId") final String chartId, //
			@Parameter(value = "chartConfiguration") final String jsonChartConfiguration //
	) throws Exception {

		final DashboardLogic logic = dashboardLogic();
		final ChartDefinition chartDefinition = mapper.readValue(jsonChartConfiguration, ChartDefinition.class);

		logic.modifyChart(dashboardId, chartId, chartDefinition);
	}

	@Admin
	@JSONExported
	public void removeChart(@Parameter(value = "dashboardId") final Long dashboardId, //
			@Parameter(value = "chartId") final String chartId) {

		final DashboardLogic logic = dashboardLogic();

		logic.removeChart(dashboardId, chartId);
	}

	@Admin
	@JSONExported
	public void moveChart(@Parameter(value = "chartId") final String chartId, //
			@Parameter(value = "fromDashboardId") final Long fromDashboardId, //
			@Parameter(value = "toDashboardId") final Long toDashboardId) {

		final DashboardLogic logic = dashboardLogic();
		logic.moveChart(chartId, fromDashboardId, toDashboardId);
	}

	@JSONExported
	public JsonResponse getChartData( //
			@Parameter(value = "dashboardId") final long dashboardId, //
			@Parameter(value = "chartId") final String chartId, //
			@Parameter(value = "params") final String jsonParams //
	) throws JsonParseException, JsonMappingException, IOException {

		final DashboardLogic logic = dashboardLogic();
		@SuppressWarnings("unchecked")
		final Map<String, Object> params = mapper.readValue(jsonParams, Map.class);
		final GetChartDataResponse result = logic.getChartData(dashboardId, chartId, params);
		return JsonResponse.success(result);
	}

	@Admin
	@JSONExported
	public JsonResponse getChartDataForPreview(@Parameter(value = "dataSourceName") final String dataSourceName,
			@Parameter(value = "params") final String jsonParams //
	) throws JsonParseException, JsonMappingException, IOException {

		final DashboardLogic logic = dashboardLogic();
		@SuppressWarnings("unchecked")
		final Map<String, Object> params = mapper.readValue(jsonParams, Map.class);
		final GetChartDataResponse result = logic.getChartData(dataSourceName, params);
		return JsonResponse.success(result);
	}
}
