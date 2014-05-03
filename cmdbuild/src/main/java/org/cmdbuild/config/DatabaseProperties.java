package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class DatabaseProperties extends DefaultProperties implements DatabaseConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "database";

	private static final String URL = "db.url";
	private static final String USERNAME = "db.username";
	private static final String PASSWORD = "db.password";
	private static final String BACKEND_CLASS = "db.backend";

	public DatabaseProperties() {
		super();
		clearConfiguration();
	}

	public static DatabaseProperties getInstance() {
		return (DatabaseProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public void clearConfiguration() {
		setProperty(URL, "");
		setProperty(USERNAME, "");
		setProperty(PASSWORD, "");
	}

	@Override
	public boolean isConfigured() {
		return !("".equals(getDatabaseUrl()) || "".equals(getDatabasePassword()) || "".equals(getDatabaseUser()));
	}

	@Override
	public String getDatabaseUrl() {
		return getProperty(URL);
	}

	@Override
	public void setDatabaseUrl(final String databaseUrl) {
		setProperty(URL, databaseUrl);
	}

	@Override
	public String getDatabaseUser() {
		return getProperty(USERNAME);
	}

	@Override
	public void setDatabaseUser(final String databaseUser) {
		setProperty(USERNAME, databaseUser);
	}

	@Override
	public String getDatabasePassword() {
		return getProperty(PASSWORD);
	}

	@Override
	public void setDatabasePassword(final String databasePassword) {
		setProperty(PASSWORD, databasePassword);
	}

	@Override
	public String getDatabaseBackendClass() {
		return getProperty(BACKEND_CLASS, "org.cmdbuild.dao.backend.postgresql.PGCMBackend");
	}

}
