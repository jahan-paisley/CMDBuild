package unit.api.fluent;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.cmdbuild.api.fluent.DownloadedReport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DownloadedReportTest {
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void returnedUrlFormatForTemporaryFileIsCorrect() throws Exception {
		final File file = temporaryFolder.newFile();
		final DownloadedReport downloadedReport = new DownloadedReport(file);
		final String urlString = downloadedReport.getUrl();
		assertThat(urlString, equalTo(expectedUrlFor(file)));
	}

	private String expectedUrlFor(final File file) {
		return format("file:%s", file.getAbsolutePath());
	}

}
