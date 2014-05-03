package org.cmdbuild.exception;

public class ReportException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final ReportExceptionType type;

	public enum ReportExceptionType {
		REPORT_COMPILE_ERROR, REPORT_LOAD_DESIGN_ERROR, REPORT_NOCLASS_ERROR, REPORT_IMPORT_ERROR, REPORT_UPLOAD_ERROR, REPORT_INVALID_FILE, REPORT_INVALID_PARAMETER_FORMAT, REPORT_INVALID_PARAMETER_CLASS, REPORT_INVALID_PARAMETER_REFERENCE_CLASS, REPORT_INVALID_PARAMETER_LOOKUP_CLASS, REPORT_INVALID_PARAMETER_VALUE, REPORT_INVISIBLE_PARAMETER, REPORT_INVALID_PARAMETER_CMDBUILD_CLASS, REPORT_INVALID_PARAMETER_CMDBUILD_ATTRIBUTE, REPORT_INVALID_PARAMETER_CMDBUILD_LOOKUP, REPORT_NOTFOUND, REPORT_GROUPNOTALLOWED;

		public ReportException createException(final String... parameters) {
			return new ReportException(this, parameters);
		}
	}

	private ReportException(final ReportExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public ReportExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
