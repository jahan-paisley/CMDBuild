package org.cmdbuild.services.database;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.utils.FileUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseConfigurator {

	public interface Configuration {

		String getHost();

		int getPort();

		String getUser();

		String getPassword();

		String getDatabaseName();

		String getDatabaseType();

		boolean useLimitedUser();

		String getLimitedUser();

		String getLimitedUserPassword();

		boolean useSharkSchema();

		String getSqlPath();

	}

	private static final String POSTGRES_SUPER_DATABASE = "postgres";
	private static final String SQL_STATE_FOR_ALREADY_PRESENT_ELEMENT = "42710";

	private static final String EXISTING_DBTYPE = "existing";
	// TODO make it private
	public static final String EMPTY_DBTYPE = "empty";
	private static final String SHARK_PASSWORD = "shark";
	private static final String SHARK_USERNAME = "shark";
	private static final String SHARK_SCHEMA = "shark";

	private static String CREATE_LANGUAGE = "CREATE LANGUAGE plpgsql";
	private static String CREATE_DATABASE = "CREATE DATABASE \"%s\" ENCODING = 'UTF8'";
	private static String CREATE_ROLE = "CREATE ROLE \"%s\" NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN ENCRYPTED PASSWORD '%s'";
	private static String CREATE_SCHEMA = "CREATE SCHEMA %s";
	private static String ALTER_DATABASE_OWNER = "ALTER DATABASE \"%s\" OWNER TO \"%s\"";
	private static String GRANT_SCHEMA_PRIVILEGES = "GRANT ALL ON SCHEMA \"%s\" TO \"%s\"";
	private static String ALTER_ROLE_PATH = "ALTER ROLE \"%s\" SET search_path=%s";

	private static final String DROP_DATABASE = "DROP DATABASE \"%s\"";

	private final String baseSqlPath;
	private final String sampleSqlPath;
	private final String sharkSqlPath;

	private final Configuration configuration;
	private final DatabaseConfiguration databaseConfiguration;
	private final PatchManager patchManager;

	public DatabaseConfigurator(final Configuration configuration,
			final DatabaseConfiguration databaseConfiguration,
			final PatchManager patchManager) {
		this.configuration = configuration;
		this.databaseConfiguration = databaseConfiguration;
		this.patchManager = patchManager;
		baseSqlPath = configuration.getSqlPath() + "base_schema"
				+ File.separator;
		sampleSqlPath = configuration.getSqlPath() + "sample_schemas"
				+ File.separator;
		sharkSqlPath = configuration.getSqlPath() + "shark_schema"
				+ File.separator;
	}

	public DataSource superDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(configuration.getUser());
		dataSource.setPassword(configuration.getPassword());
		dataSource.setDatabaseName(POSTGRES_SUPER_DATABASE);
		return dataSource;
	}

	public DataSource systemDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(getSystemUser());
		dataSource.setPassword(getSystemPassword());
		dataSource.setDatabaseName(configuration.getDatabaseName());
		return dataSource;
	}

	private DataSource sharkDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(SHARK_USERNAME);
		dataSource.setPassword(SHARK_PASSWORD);
		dataSource.setDatabaseName(configuration.getDatabaseName());
		return dataSource;
	}

	private boolean needsLimitedUser() {
		return (configuration.getLimitedUser() != null)
				&& (configuration.getLimitedUserPassword() != null);
	}

	private String getSystemUser() {
		if (needsLimitedUser()) {
			return configuration.getLimitedUser();
		} else {
			return configuration.getUser();
		}
	}

	private String getSystemPassword() {
		if (needsLimitedUser()) {
			return configuration.getLimitedUserPassword();
		} else {
			return configuration.getPassword();
		}
	}

	/*
	 * Configure
	 */

	public void configureAndSaveSettings() {
		configure(true);
	}

	public void configureAndDoNotSaveSettings() {
		configure(false);
	}

	private void configure(final boolean saveSettings) {
		try {
			prepareConfiguration();
			createDatabaseIfNeeded();
			fillDatabaseIfNeeded();
			if (saveSettings) {
				saveConfiguration();
			}
			addLastPatchIfEmptyDb();
		} catch (final Exception e) {
			clearConfiguration();
			Log.SQL.error(
					"Error while configuring the database. Exception message is {}",
					e.getMessage());
			Log.SQL.error("Caused by {}", e.getCause().getMessage());
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

	private void addLastPatchIfEmptyDb() {
		Log.CMDBUILD
				.info("inside addLastPatchIfEmptyDb method but before if. Database type: "
						+ configuration.getDatabaseType());
		if (EMPTY_DBTYPE.equals(configuration.getDatabaseType())) {
			Log.CMDBUILD
					.info("Before adding last patch to the empty database...");
			patchManager.createLastPatch();
		}
	}

	private void createDatabaseIfNeeded() {
		if (!EXISTING_DBTYPE.equals(configuration.getDatabaseType())) {
			createDatabase(configuration.getDatabaseName());
		}
	}

	private void createDatabase(final String name) {
		Log.CMDBUILD.info("Creating database " + name);
		new JdbcTemplate(superDataSource()).execute(String.format(
				CREATE_DATABASE, escapeSchemaName(name)));
	}

	private void createPLSQLLanguage() {
		Log.CMDBUILD.info("Creating PL/SQL language");
		try {
			new JdbcTemplate(superDataSource()).execute(CREATE_LANGUAGE);
		} catch (final DataAccessException e) {
			forwardIfNotAlreadyPresentElement(e);
		}
	}

	private void fillDatabaseIfNeeded() {
		if (!EXISTING_DBTYPE.equals(configuration.getDatabaseType())) {
			createSystemRoleIfNeeded();
			alterDatabaseOwnerIfNeeded();
			createPLSQLLanguage();
			if (EMPTY_DBTYPE.equals(configuration.getDatabaseType())) {
				createCmdbuildStructure();
			} else {
				restoreSampleDB();
			}
			if (configuration.useSharkSchema()) {
				createSharkRole();
				createSchema(SHARK_SCHEMA);
				grantSchemaPrivileges(SHARK_SCHEMA, SHARK_USERNAME);
				createSharkTables();
			}
		}
	}

	private void alterDatabaseOwnerIfNeeded() {
		if (needsLimitedUser())
			alterDatabaseOwner(configuration.getDatabaseName(), getSystemUser());
	}

	private void restoreSampleDB() {
		Log.CMDBUILD.info("Restoring demo structure");
		final String filename = sampleSqlPath + configuration.getDatabaseType()
				+ "_schema.sql";
		final String sql = FileUtils.getContents(filename);
		new JdbcTemplate(systemDataSource()).execute(sql);
	}

	private void createSharkTables() {
		Log.CMDBUILD.info("Creating shark tables");
		new JdbcTemplate(sharkDataSource()).execute(FileUtils
				.getContents(sharkSqlPath + "02_shark_emptydb.sql"));
	}

	private void createSchema(final String schema) {
		new JdbcTemplate(systemDataSource()).execute(String.format(
				CREATE_SCHEMA, escapeSchemaName(schema)));
	}

	private void createCmdbuildStructure() {
		Log.CMDBUILD.info("Creating CMDBuild structure");
		final List<String> sqlFiles = Arrays.asList( //
				baseSqlPath + "01_system_functions_base.sql", //
				baseSqlPath + "02_system_functions_class.sql", //
				baseSqlPath + "03_system_functions_attribute.sql", //
				baseSqlPath + "04_system_functions_domain.sql", //
				baseSqlPath + "05_base_tables.sql", //
				baseSqlPath + "06_system_views_base.sql", //
				baseSqlPath + "07_support_tables.sql", //
				baseSqlPath + "08_user_tables.sql", //
				baseSqlPath + "09_system_views_extras.sql", //
				baseSqlPath + "10_system_functions_extras.sql", //
				baseSqlPath + "11_workflow.sql", //
				baseSqlPath + "12_tecnoteca_extras.sql", //
				baseSqlPath + "13_bim.sql"
		);
		for (final String file : sqlFiles) {
			Log.CMDBUILD.info("applying '{}'", file);
			final String content = FileUtils.getContents(file);
			new JdbcTemplate(systemDataSource()).execute(content);
		}
	}

	private void createSystemRoleIfNeeded() {
		if (configuration.useLimitedUser())
			createRole(configuration.getLimitedUser(),
					configuration.getLimitedUserPassword());
	}

	private void alterDatabaseOwner(final String database, final String role) {
		Log.CMDBUILD.info("Changing database ownership");
		new JdbcTemplate(superDataSource()).execute(String.format(
				ALTER_DATABASE_OWNER, escapeSchemaName(database),
				escapeSchemaName(role)));
	}

	private void grantSchemaPrivileges(final String schema, final String role) {
		Log.CMDBUILD.info("Granting schema privileges");
		new JdbcTemplate(systemDataSource()).execute(String.format(
				GRANT_SCHEMA_PRIVILEGES, escapeSchemaName(schema),
				escapeSchemaName(role)));
	}

	private void createRole(final String roleName, final String rolePassword) {
		Log.CMDBUILD.info("Creating role " + roleName);
		new JdbcTemplate(superDataSource()).execute(String.format(CREATE_ROLE,
				escapeSchemaName(roleName), escapeValue(rolePassword)));
	}

	private void createSharkRole() {
		Log.CMDBUILD.info("Creating shark role");
		try {
			final JdbcTemplate jdbcTemplate = new JdbcTemplate(
					superDataSource());
			jdbcTemplate.execute(String.format(CREATE_ROLE, SHARK_USERNAME,
					SHARK_PASSWORD));
			jdbcTemplate.execute(String.format(ALTER_ROLE_PATH, SHARK_USERNAME,
					"pg_default,shark"));
		} catch (final DataAccessException e) {
			forwardIfNotAlreadyPresentElement(e);
		}
	}

	private void prepareConfiguration() throws IOException {
		databaseConfiguration.setDatabaseUrl(String.format(
				"jdbc:postgresql://%1$s:%2$s/%3$s", configuration.getHost(),
				configuration.getPort(), configuration.getDatabaseName()));
		databaseConfiguration.setDatabaseUser(getSystemUser());
		databaseConfiguration.setDatabasePassword(getSystemPassword());
	}

	private void saveConfiguration() throws IOException {
		Log.CMDBUILD.info("Saving configuration");
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.store();
	}

	private void clearConfiguration() {
		databaseConfiguration.clearConfiguration();
	}

	/*
	 * We don't know what could go wrong if this is allowed
	 */
	private String escapeSchemaName(final String name) {
		if (name.indexOf('"') >= 0) {
			throw ORMExceptionType.ORM_ILLEGAL_NAME_ERROR.createException(name);
		}
		return name;
	}

	private String escapeValue(final String value) {
		return value.replaceAll("'", "''");
	}

	private void forwardIfNotAlreadyPresentElement(final DataAccessException e) {
		final Throwable cause = e.getCause();
		if (cause instanceof SQLException) {
			final String sqlState = SQLException.class.cast(cause)
					.getSQLState();
			if (!SQL_STATE_FOR_ALREADY_PRESENT_ELEMENT.equals(sqlState)) {
				throw e;
			} else {
				// TODO log
			}
		} else {
			throw e;
		}
	}

	public void drop() {
		String dbName = configuration.getDatabaseName();
		new JdbcTemplate(superDataSource()).execute(String.format(
				DROP_DATABASE, escapeSchemaName(dbName)));
	}

}
