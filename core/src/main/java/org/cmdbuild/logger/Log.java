package org.cmdbuild.logger;

import org.cmdbuild.dms.DmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

	private Log() {
	}

	public static final Logger PERSISTENCE = org.cmdbuild.dao.logging.LoggingSupport.logger;
	public static final Logger SQL = org.cmdbuild.dao.driver.postgres.logging.LoggingSupport.sqlLogger;
	public static final Logger DDSQL = org.cmdbuild.dao.driver.postgres.logging.LoggingSupport.dataDefinitionSqlLogger;
	public static final Logger WORKFLOW = LoggerFactory.getLogger("workflow");
	public static final Logger JSONRPC = LoggerFactory.getLogger("jsonrpc");
	public static final Logger SOAP = LoggerFactory.getLogger("soap");
	public static final Logger DMS = DmsService.LoggingSupport.logger;
	public static final Logger REST = LoggerFactory.getLogger("rest");
	public static final Logger REPORT = LoggerFactory.getLogger("report");
	public static final Logger EMAIL = LoggerFactory.getLogger("email");
	public static final Logger AUTH = org.cmdbuild.auth.logging.LoggingSupport.logger;
	public static final Logger CMDBUILD = LoggerFactory.getLogger("cmdbuild");

}
