package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class SetupTranslations extends JSONBaseWithSpringContext {
	static Map<String, String> translations = new HashMap<String, String>();
	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration( //
	) throws JSONException, AuthException {
		final JSONObject out = new JSONObject();
		final JSONObject data = new JSONObject();

		for (final Object keyObject : translations.keySet()) {
			final String key = keyObject.toString();
			data.put(key, translations.get(key));
		}
		out.put("data", data);
		return out;
	}

	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveConfiguration( //
			final Map<String, String> requestParams //
		) throws IOException {

		for (final Object keyObject : requestParams.keySet()) {
			final String key = keyObject.toString();
			String value = requestParams.get(key);
			translations.put(key, value);
		}
	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveClass( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("class name = " + className);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveClassAttribute( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = "attributeName") final String attributeName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("class name = " + className);
		System.out.println("attribute name = " + attributeName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveDomain( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("domain name = " + domainName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveDomainAttribute( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = "attributeName") final String attributeName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("domain name = " + domainName);
		System.out.println("attribute name = " + attributeName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveFilterView( //
			@Parameter(value = "viewName") final String viewName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("FilterviewName = " + viewName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveSqlView( //
			@Parameter(value = "viewName") final String viewName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("SQLviewName = " + viewName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveFilter( //
			@Parameter(value = "filterName") final String filterName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("filterName = " + filterName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveInstanceName( //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("instanceName translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveWidget( //
			@Parameter(value = WIDGET_ID) final String widgetId, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("widgetId = " + widgetId);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveDashboard( //
			@Parameter(value = "dashboardName") final String dashboardName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("dashboardName = " + dashboardName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveChart( //
			@Parameter(value = "chartName") final String chartName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("chartName = " + chartName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveReport( //
			@Parameter(value = "reportName") final String reportName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("reportName = " + reportName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveLookup( //
			@Parameter(value = "lookupId") final String lookupId, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("lookupId = " + lookupId);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}
	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveGisIcon( //
			@Parameter(value = "iconName") final String iconName, //
			@Parameter(value = "field") final String field, //
			@Parameter(value = "translations") final JSONObject translations //
		) throws IOException {
		System.out.println("iconName = " + iconName);
		System.out.println("field = " + field);
		System.out.println("translations = " + translations);

	}

}