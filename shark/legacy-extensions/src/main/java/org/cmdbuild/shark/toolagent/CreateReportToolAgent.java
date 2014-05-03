package org.cmdbuild.shark.toolagent;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.DownloadedReport;

public class CreateReportToolAgent extends AbstractConditionalToolAgent {

	private static final String REPORT_TITLE = "Code";
	private static final String REPORT_FORMAT = "Format";
	private static final String REPORT_DOWNLOADED_URL = "ReportURL";

	@Override
	protected void innerInvoke() throws Exception {
		final String title = getExtendedAttribute(REPORT_TITLE);
		final String format = getExtendedAttribute(REPORT_FORMAT);
		final Map<String, Object> attributes = getInputParameterValues();

		final CreateReport createReport = getWorkflowApi().createReport(title, format);
		for (final Entry<String, Object> attributeEntry : attributes.entrySet()) {
			createReport.with(attributeEntry.getKey(), attributeEntry.getValue());
		}
		final DownloadedReport downloadedReport = createReport.download();

		setParameterValue(REPORT_DOWNLOADED_URL, downloadedReport.getUrl());
	}

}
