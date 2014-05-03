package org.cmdbuild.model.widget;

import java.util.Map;

public class OpenReport extends Widget {

	private String reportCode;
	private String forceFormat;
	private Map<String, Object> preset;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setReportCode(final String reportCode) {
		this.reportCode = reportCode;
	}

	public String getReportCode() {
		return reportCode;
	}

	public void setForceFormat(final String forceFormat) {
		this.forceFormat = forceFormat;
	}

	public String getForceFormat() {
		return forceFormat;
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}
}
