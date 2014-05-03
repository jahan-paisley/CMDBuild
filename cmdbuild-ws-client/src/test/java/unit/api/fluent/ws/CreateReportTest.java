package unit.api.fluent.ws;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;

import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.services.soap.Report;
import org.cmdbuild.services.soap.ReportParams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

public class CreateReportTest extends AbstractWsFluentApiTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static final int REPORT_ID = 12345;
	private static final String REPORT_TITLE = randomString();
	private static final String REPORT_FORMAT = "xyz";

	private static final String PARAMETER_1 = "foo";
	private static final String PARAMETER_1_VALUE = randomString();
	private static final String PARAMETER_2 = "bar";
	private static final String PARAMETER_2_VALUE = randomString();

	private CreateReport createReport;
	private URL url;

	@Before
	public void createExistingCard() throws Exception {
		createReport = api() //
				.createReport(REPORT_TITLE, REPORT_FORMAT) // \
				.with(PARAMETER_1, PARAMETER_1_VALUE) //
				.with(PARAMETER_2, PARAMETER_2_VALUE);
	}

	@Before
	public void createTemporaryUrl() throws Exception {
		url = temporaryFolder.newFile().toURI().toURL();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyCalledAsExpected() throws Exception {
		when(proxy().getReportList(anyString(), anyInt(), anyInt())) //
				.thenReturn(asList(report(REPORT_ID, REPORT_TITLE)));
		when(proxy().getReport(eq(REPORT_ID), eq(REPORT_FORMAT), anyList())) //
				.thenReturn(dataHandlerFor(url));

		createReport.download();

		final InOrder inOrder = inOrder(proxy());
		inOrder.verify(proxy()).getReportList("custom", Integer.MAX_VALUE, 0);
		inOrder.verify(proxy()).getReportParameters(REPORT_ID, REPORT_FORMAT);
		inOrder.verify(proxy()).getReport(eq(REPORT_ID), eq(REPORT_FORMAT), anyList());
		verifyNoMoreInteractions(proxy());
	}

	private DataHandler dataHandlerFor(final URL url) {
		return new DataHandler(url);
	}

	private Report report(final int id, final String title) {
		final Report report = new Report();
		report.setId(id);
		report.setTitle(title);
		return report;
	}

	@Test
	public void returnedUrlIsValidFile() throws Exception {
		when(proxy().getReportList(anyString(), anyInt(), anyInt())) //
				.thenReturn(asList(report(REPORT_ID, REPORT_TITLE)));
		when(proxy().getReport(eq(REPORT_ID), eq(REPORT_FORMAT), anyListOf(ReportParams.class))) //
				.thenReturn(dataHandlerFor(url));

		final DownloadedReport downloadedReport = createReport.download();
		final URL downloadedUrl = new URL(downloadedReport.getUrl());
		final File downloadedFile = new File(downloadedUrl.getFile());

		assertThat(downloadedFile.exists(), equalTo(true));
	}

}
