package org.cmdbuild.api.fluent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadedReport {

	private final URL url;

	public DownloadedReport(final URL url) {
		this.url = url;
	}

	public DownloadedReport(final File file) {
		try {
			this.url = file.toURI().toURL();
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String getUrl() {
		return url.toString();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof DownloadedReport)) {
			return false;
		}
		final DownloadedReport downloadedReport = DownloadedReport.class.cast(object);
		return url.equals(downloadedReport.url);
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

}
