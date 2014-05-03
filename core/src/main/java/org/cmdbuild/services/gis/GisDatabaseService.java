package org.cmdbuild.services.gis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.cmdbuild.logger.Log;

public class GisDatabaseService {

	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;

	private static String postgisVersion;

	private final DataSource datasource;

	public GisDatabaseService(final DataSource dataSource) {
		this.datasource = dataSource;
	}

	// TODO: Move it to the driver implementation
	public static String getDriverVersion() {
		try {
			// Needs to read it from the current classpath, thus we can't use
			// the field reference directly!
			final int major = DRIVER_CLASS.getField("MAJORVERSION").getInt(null);
			final int minor = DRIVER_CLASS.getField("MINORVERSION").getInt(null);
			final int build = org.postgresql.util.PSQLDriverVersion.class.getField("buildNumber").getInt(null);
			return String.format("%d.%d-%d", major, minor, build);
		} catch (final Exception e) {
			return "undefined";
		}
	}

	public String getPostGISVersion() {
		if (postgisVersion == null) {
			synchronized (this) {
				if (postgisVersion == null) {
					postgisVersion = fetchPostGISVersion();
				}
			}
		}

		return postgisVersion;
	}

	private String fetchPostGISVersion() {
		try {
			final Connection c = datasource.getConnection();
			final Statement s = c.createStatement();
			final ResultSet r = s.executeQuery("select postgis_lib_version()");
			if (r.next()) {
				final String postgisVersion = r.getString(1);
				Log.SQL.info("PostGIS version is " + postgisVersion);
				return postgisVersion;
			}
		} catch (final SQLException ex) {
			Log.SQL.error("PostGIS is not installed", ex);
		}

		return null;
	}

	public boolean isPostGISConfigured() {
		return getPostGISVersion() != null;
	}

}
