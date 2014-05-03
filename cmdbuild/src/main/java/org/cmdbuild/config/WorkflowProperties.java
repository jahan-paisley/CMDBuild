package org.cmdbuild.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.services.Settings;
import org.cmdbuild.workflow.service.RemoteSharkServiceConfiguration;

public class WorkflowProperties extends DefaultProperties implements WorkflowConfiguration {

	private static final long serialVersionUID = 8184420208391927123L;

	private static final String MODULE_NAME = "workflow";

	private static final String ENABLED = "enabled";
	private static final String ENDPOINT = "endpoint";
	private static final String ADMIN_USERNAME = "user";
	private static final String ADMIN_PASSWORD = "password";

	private final Set<ChangeListener> changeListeners;

	public WorkflowProperties() {
		super();

		changeListeners = new HashSet<RemoteSharkServiceConfiguration.ChangeListener>();

		setProperty(ENABLED, String.valueOf(false));
		setProperty(ENDPOINT, "http://localhost:8080/shark");
		setProperty(ADMIN_USERNAME, "admin");
		setProperty(ADMIN_PASSWORD, "enhydra");
	}

	public static WorkflowProperties getInstance() {
		return (WorkflowProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		final String enabled = getProperty(ENABLED, Boolean.FALSE.toString());
		return Boolean.parseBoolean(enabled);
	}

	@Override
	public String getServerUrl() {
		return getProperty(ENDPOINT);
	}

	@Override
	public String getUsername() {
		return getProperty(ADMIN_USERNAME);
	}

	@Override
	public String getPassword() {
		return getProperty(ADMIN_PASSWORD);
	}

	@Override
	public void addListener(final ChangeListener listener) {
		changeListeners.add(listener);
	}

	@Override
	public void store() throws IOException {
		super.store();
		notifyListeners();
	}

	private void notifyListeners() {
		for (final ChangeListener changeListener : changeListeners) {
			changeListener.configurationChanged();
		}
	}

}
