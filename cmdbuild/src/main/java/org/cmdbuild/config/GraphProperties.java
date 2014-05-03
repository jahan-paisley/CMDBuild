package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class GraphProperties extends DefaultProperties {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "graph";

	private static final String BASE_LEVEL = "baseLevel";
	private static final String ENABLED = "enabled";
	private static final String EXTENSION_MAXIMUM_LEVEL = "extensionMaximumLevel";
	private static final String CLUSTERING_THRESHOLD = "clusteringThreshold";
	private static final String EXPANDING_THRESHOLD = "expandingThreshold";

	public GraphProperties() {
		super();
		setProperty(BASE_LEVEL, "1");
		setProperty(EXTENSION_MAXIMUM_LEVEL, "5");
		setProperty(CLUSTERING_THRESHOLD, "5");
		setProperty(EXPANDING_THRESHOLD, "30");
		setProperty(ENABLED, Boolean.TRUE.toString());
	}

	public static GraphProperties getInstance() {
		return (GraphProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public int getClusteringThreshold() {
		final String clusteringThreshold = getProperty(CLUSTERING_THRESHOLD);
		return Integer.parseInt(clusteringThreshold);
	}

	public int getExpandingThreshold() {
		final String expandingThreshold = getProperty(EXPANDING_THRESHOLD);
		return Integer.parseInt(expandingThreshold);
	}

}
