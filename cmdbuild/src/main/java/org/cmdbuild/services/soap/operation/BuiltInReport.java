package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.report.ReportFactory;

public enum BuiltInReport {

	LIST("_list") {

		@Override
		public ReportFactoryBuilder<ReportFactory> newBuilder(final CMDataView dataView,
				final AuthenticationStore authenticationStore, final CmdbuildConfiguration configuration) {
			return new ListReportFactoryBuilder(dataView, authenticationStore, configuration);
		}

	},
	;

	private final String reportId;

	private BuiltInReport(final String reportId) {
		this.reportId = reportId;
	}

	public static BuiltInReport from(final String reportId) {
		for (final BuiltInReport report : values()) {
			if (report.reportId.equals(reportId)) {
				return report;
			}
		}
		throw new Error(format("undefined report '%s'", reportId));
	}

	public abstract ReportFactoryBuilder<ReportFactory> newBuilder(CMDataView dataView,
			AuthenticationStore authenticationStore, CmdbuildConfiguration configuration);

}
