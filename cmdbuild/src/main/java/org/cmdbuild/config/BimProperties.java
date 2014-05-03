package org.cmdbuild.config;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;

import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.services.Settings;

public class BimProperties extends DefaultProperties implements BimserverConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "bim", ENABLED = "enabled", URL = "url", USERNAME = "username",
			PASSWORD = "password";

	private ChangeListener listener;

	public BimProperties() {
		super();
		setProperty(ENABLED, "false");
		setProperty(URL, EMPTY);
		setProperty(USERNAME, EMPTY);
		setProperty(PASSWORD, EMPTY);
	}

	public static BimProperties getInstance() {
		return (BimProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return Boolean.parseBoolean(getProperty(ENABLED));
	}

	@Override
	public void store() throws IOException {
		super.store();
		notifyListener();
	}

	@Override
	public String getUsername() {
		return getProperty(USERNAME);
	}

	@Override
	public String getPassword() {
		return getProperty(PASSWORD);
	}

	@Override
	public String getUrl() {
		return getProperty(URL);
	}

	@Override
	public void addListener(final ChangeListener listener) {
		this.listener = listener;
	}

	private void notifyListener() {
		listener.configurationChanged();
	}

	@Override
	public void disable() {
		setProperty(ENABLED, "false");
		try {
			store();
		} catch (IOException e) {
			// TODO: Log me
		}
	}

}
