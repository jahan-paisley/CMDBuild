package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPLookup extends ReportParameter {

	protected RPLookup(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")
				|| !getFullName().matches(regExpLR)) {

			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}

		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_LOOKUP_CLASS.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	public String getLookupName() {
		return getFullNameSplit()[2];
	}

	@Override
	public void parseValue(final String value) {
		if (value != null && !value.equals("")) {
			setValue(Integer.parseInt(value));
		}
	}

}
