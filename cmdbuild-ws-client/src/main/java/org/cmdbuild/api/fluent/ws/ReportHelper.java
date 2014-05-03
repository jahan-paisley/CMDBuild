package org.cmdbuild.api.fluent.ws;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Report;
import org.cmdbuild.services.soap.ReportParams;

class ReportHelper {

	public static final String DEFAULT_TYPE = "custom";
	public static final String TEMPORARY_FILE_PREFIX = "report";

	private final Private proxy;

	public ReportHelper(final Private proxy) {
		this.proxy = proxy;
	}

	public List<Report> getReports(final String type) {
		return proxy.getReportList(type, Integer.MAX_VALUE, 0);
	}

	public Report getReport(final String type, final String title) {
		final List<Report> reports = getReports(type);
		for (final Report report : reports) {
			if (report.getTitle().equals(title)) {
				return report;
			}
		}
		final String message = format("missing report for type '%s' and title '%s'", type, title);
		throw new IllegalArgumentException(message);
	}

	public List<AttributeSchema> getParamSchemas(final Report report, final String format) {
		return proxy.getReportParameters(report.getId(), format);
	}

	public DataHandler getDataHandler(final Report report, final String format, final List<ReportParams> reportParams) {
		final int id = report.getId();
		return proxy.getReport(id, format, reportParams);
	}

	public File temporaryFile(final String name, String format) {
		try {
			if (!format.isEmpty()) {
				format = "." + format;
			}
			final File file = File.createTempFile(name, format);
			file.deleteOnExit();
			return file;
		} catch (final IOException e) {
			final String message = "error creating temporary file";
			throw new IllegalArgumentException(message);
		}
	}

	public void saveToFile(final DataHandler dataHandler, final File file) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
		} catch (final IOException e) {
			final String message = format("error saving report to file '%s'", file.getAbsolutePath());
			throw new IllegalArgumentException(message);
		} finally {
			try {
				outputStream.close();
			} catch (final IOException e) {
				final String message = format("error closing stream");
				throw new IllegalArgumentException(message);
			}
		}
	}

}
