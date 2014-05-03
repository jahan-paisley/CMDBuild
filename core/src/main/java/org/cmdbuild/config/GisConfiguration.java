package org.cmdbuild.config;

public interface GisConfiguration {

	boolean isEnabled();

	boolean isGeoServerEnabled();

	boolean isServiceOn(String service);

	String getGoogleKey();

	String getYahooKey();

	String getGeoServerUrl();

	String getGeoServerWorkspace();

	String getGeoServerAdminUser();

	String getGeoServerAdminPassword();

}
