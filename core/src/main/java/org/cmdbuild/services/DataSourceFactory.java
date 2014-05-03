package org.cmdbuild.services;

import javax.sql.DataSource;

public interface DataSourceFactory {

	DataSource create();

}
