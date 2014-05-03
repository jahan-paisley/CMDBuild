package org.cmdbuild.config;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.gis.GisDatabaseService;

public class GisProperties extends DefaultProperties implements GisConfiguration {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ZOOM = 24;
	private static final int MIN_ZOOM = 0;

	private static final String MODULE_NAME = "gis";

	private static final String ENABLED = "enabled";
	private static final String CENTER_LON = "center.lon";
	private static final String CENTER_LAT = "center.lat";
	private static final String INITIAL_ZOOM_LEVEL = "initialZoomLevel";

	public static final String YAHOO = "yahoo";
	private static final String YAHOO_KEY = "yahoo_key";
	private static final String YAHOO_MAXZOOM = "yahoo_maxzoom";
	private static final String YAHOO_MINZOOM = "yahoo_minzoom";

	public static final String GOOGLE = "google";
	private static final String GOOGLE_KEY = "google_key";
	private static final String GOOGLE_MAXZOOM = "google_maxzoom";
	private static final String GOOGLE_MINZOOM = "google_minzoom";

	public static final String GEOSERVER = "geoserver";
	public static final String GEOSERVER_URL = "geoserver_url";
	public static final String GEOSERVER_WORKSPACE = "geoserver_workspace";
	public static final String GEOSERVER_ADMIN_USER = "geoserver_admin_user";
	public static final String GEOSERVER_ADMIN_PASSWORD = "geoserver_admin_password";
	private static final String GEOSERVER_MAXZOOM = "geoserver_maxzoom";
	private static final String GEOSERVER_MINZOOM = "geoserver_minzoom";

	public static final String OSM = "osm";
	private static final String OSM_MAXZOOM = "osm_maxzoom";
	private static final String OSM_MINZOOM = "osm_minzoom";

	public GisProperties() {
		setProperty(ENABLED, Boolean.FALSE.toString());

		setProperty(CENTER_LON, String.valueOf(0));
		setProperty(CENTER_LAT, String.valueOf(0));
		setProperty(INITIAL_ZOOM_LEVEL, String.valueOf(3));

		setProperty(YAHOO, "off");
		setProperty(YAHOO_KEY, "");
		setProperty(YAHOO_MAXZOOM, String.valueOf(MAX_ZOOM));
		setProperty(YAHOO_MINZOOM, String.valueOf(MIN_ZOOM));

		setProperty(GOOGLE, "off");
		setProperty(GOOGLE_KEY, "");
		setProperty(GOOGLE_MAXZOOM, String.valueOf(MAX_ZOOM));
		setProperty(GOOGLE_MINZOOM, String.valueOf(MIN_ZOOM));

		setProperty(GEOSERVER, "off");
		setProperty(GEOSERVER_URL, "");
		setProperty(GEOSERVER_WORKSPACE, "cmdbuild");
		setProperty(GEOSERVER_ADMIN_USER, "");
		setProperty(GEOSERVER_ADMIN_PASSWORD, "");
		setProperty(GEOSERVER_MAXZOOM, String.valueOf(MAX_ZOOM));
		setProperty(GEOSERVER_MINZOOM, String.valueOf(MIN_ZOOM));

		setProperty(OSM, "on");
		setProperty(OSM_MAXZOOM, String.valueOf(MAX_ZOOM));
		setProperty(OSM_MINZOOM, String.valueOf(MIN_ZOOM));
	}

	@Override
	protected Object setProperty0(final String key, final String value) {
		if (ENABLED.equals(key) && Boolean.TRUE.toString().equalsIgnoreCase(value)) {
			final GisDatabaseService gisDatabaseService = applicationContext().getBean(GisDatabaseService.class);
			if (!gisDatabaseService.isPostGISConfigured()) {
				throw ORMExceptionType.ORM_POSTGIS_NOT_FOUND.createException();
			}
		}
		return super.setProperty0(key, value);
	}

	public static GisProperties getInstance() {
		return (GisProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		final String enabled = getProperty(ENABLED);
		return enabled.equals("true");
	}

	@Override
	public boolean isGeoServerEnabled() {
		return isServiceOn(GEOSERVER);
	}

	@Override
	public boolean isServiceOn(final String service) {
		final String enabled = getProperty(service);
		return "on".equals(enabled);
	}

	@Override
	public String getGoogleKey() {
		return getProperty(GOOGLE_KEY);
	}

	@Override
	public String getYahooKey() {
		return getProperty(YAHOO_KEY);
	}

	@Override
	public String getGeoServerUrl() {
		String url = getProperty(GEOSERVER_URL);
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	@Override
	public String getGeoServerWorkspace() {
		return getProperty(GEOSERVER_WORKSPACE);
	}

	@Override
	public String getGeoServerAdminUser() {
		return getProperty(GEOSERVER_ADMIN_USER);
	}

	@Override
	public String getGeoServerAdminPassword() {
		return getProperty(GEOSERVER_ADMIN_PASSWORD);
	}

}
