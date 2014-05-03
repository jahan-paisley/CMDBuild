package org.cmdbuild.config;

public interface DatabaseConfiguration {

	void clearConfiguration();

	boolean isConfigured();

	String getDatabaseUrl();

	void setDatabaseUrl(String databaseUrl);

	String getDatabaseUser();

	void setDatabaseUser(String databaseUser);

	String getDatabasePassword();

	void setDatabasePassword(String databasePassword);

	String getDatabaseBackendClass();

}
