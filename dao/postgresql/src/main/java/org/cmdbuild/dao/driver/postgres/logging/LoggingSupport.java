package org.cmdbuild.dao.driver.postgres.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LoggingSupport {

	Logger sqlLogger = LoggerFactory.getLogger("sql");

	Logger dataDefinitionSqlLogger = LoggerFactory.getLogger("ddsql");

}
