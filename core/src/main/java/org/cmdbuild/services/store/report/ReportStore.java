package org.cmdbuild.services.store.report;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.cmdbuild.exception.ORMException;
import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory.ReportType;

public interface ReportStore {
	List<Report> findReportsByType(final ReportType type) throws ORMException;

	Report findReportByTypeAndCode(final ReportType type, final String code) throws ORMException;

	Report findReportById(final int id);

	void deleteReport(final int id);

	List<String> getReportTypes();

	void insertReport(Report report) throws SQLException, IOException;

	void updateReport(Report report) throws SQLException, IOException;
}
