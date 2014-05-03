package org.cmdbuild.services;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.cmdbuild.config.DatabaseConfiguration;

public class DefaultDataSourceFactory implements DataSourceFactory {

	private static final String DATASOURCE_NAME = "jdbc/cmdbuild";
	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;

	private static class DefaultDataSource implements DataSource {

		private final DatabaseConfiguration configuration;
		private final BasicDataSource dataSource;
		private final Boolean configured = new Boolean(false);

		public DefaultDataSource(final DatabaseConfiguration configuration, final BasicDataSource dataSource) {
			this.configuration = configuration;
			this.dataSource = dataSource;
		}

		private DataSource configureDatasource() {
			if (!configuration.isConfigured()) {
				throw new IllegalStateException("database connection not configured");
			}
			dataSource.setDriverClassName(DRIVER_CLASS.getCanonicalName());
			dataSource.setUrl(configuration.getDatabaseUrl());
			dataSource.setUsername(configuration.getDatabaseUser());
			dataSource.setPassword(configuration.getDatabasePassword());
			return dataSource;
		}

		@Override
		public Connection getConnection() throws SQLException {
			if (!configured.booleanValue()) {
				synchronized (configured) {
					if (!configured.booleanValue()) {
						configureDatasource();
					}
				}
			}
			return dataSource.getConnection();
		}

		@Override
		public Connection getConnection(final String username, final String password) throws SQLException {
			return dataSource.getConnection(username, password);
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return dataSource.getLogWriter();
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return dataSource.getLoginTimeout();
		}

		@Override
		public void setLogWriter(final PrintWriter out) throws SQLException {
			dataSource.setLogWriter(out);
		}

		@Override
		public void setLoginTimeout(final int seconds) throws SQLException {
			dataSource.setLoginTimeout(seconds);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T unwrap(final Class<T> iface) throws SQLException {
			Validate.notNull(iface, "Interface argument must not be null");
			if (!DataSource.class.equals(iface)) {
				final String message = format("data source of type '%s' can only be unwrapped as '%s', not as '%s'", //
						getClass().getName(), //
						DataSource.class.getName(), //
						iface.getName());
				throw new SQLException(message);
			}
			return (T) this;
		}

		@Override
		public boolean isWrapperFor(final Class<?> iface) throws SQLException {
			return DataSource.class.equals(iface);
		}

	}

	private final DatabaseConfiguration configuration;

	public DefaultDataSourceFactory(final DatabaseConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public DataSource create() {
		BasicDataSource dataSource;
		try {
			final InitialContext ictx = new InitialContext();
			final Context ctx = (Context) ictx.lookup("java:/comp/env");
			dataSource = (BasicDataSource) ctx.lookup(DATASOURCE_NAME);
		} catch (final NamingException e) {
			dataSource = new BasicDataSource();
		}
		return new DefaultDataSource(configuration, dataSource);
	}

}
