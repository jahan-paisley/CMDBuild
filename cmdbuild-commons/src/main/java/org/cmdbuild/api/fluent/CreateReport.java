package org.cmdbuild.api.fluent;

public class CreateReport extends ActiveReport {

	CreateReport(final FluentApi api, final String title, final String format) {
		super(api, title, format);
	}

	public CreateReport with(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public DownloadedReport download() {
		return getApi().getExecutor().download(this);
	}

}
