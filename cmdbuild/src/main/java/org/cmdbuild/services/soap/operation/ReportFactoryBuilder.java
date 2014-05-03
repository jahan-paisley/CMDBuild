package org.cmdbuild.services.soap.operation;

import java.util.Map;

import javax.sql.DataSource;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.report.ReportFactory;

public interface ReportFactoryBuilder<T extends ReportFactory> {

	ReportFactoryBuilder<ReportFactory> withOperationUser(OperationUser operationUser);

	ReportFactoryBuilder<ReportFactory> withDataSource(DataSource dataSource);

	ReportFactoryBuilder<ReportFactory> withDataAccessLogic(DataAccessLogic dataAccessLogic);

	ReportFactoryBuilder<T> withExtension(String extension);

	ReportFactoryBuilder<T> withProperties(Map<String, String> properties);

	T build();

}
