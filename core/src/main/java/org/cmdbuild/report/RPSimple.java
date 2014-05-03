package org.cmdbuild.report;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.common.Constants;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;

public class RPSimple extends ReportParameter {

	protected RPSimple(final JRParameter jrParameter) {
		super();
		setJrParameter(jrParameter);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	@Override
	public void parseValue(final String newValue) {
		try {
			if (newValue != null && !newValue.equals("")) {

				if (getJrParameter().getValueClass() == String.class) {
					setValue(newValue);
				} else if (getJrParameter().getValueClass() == Integer.class
						|| getJrParameter().getValueClass() == Number.class) {
					setValue(Integer.parseInt(newValue));
				} else if (getJrParameter().getValueClass() == Long.class) {
					setValue(Long.parseLong(newValue));
				} else if (getJrParameter().getValueClass() == Short.class) {
					setValue(Short.parseShort(newValue));
				} else if (getJrParameter().getValueClass() == BigDecimal.class) {
					setValue(new BigDecimal(Integer.parseInt(newValue)));
				} else if (getJrParameter().getValueClass() == Date.class) {
					setValue(new SimpleDateFormat(Constants.DATE_TWO_DIGIT_YEAR_FORMAT).parse(newValue));
				} else if (getJrParameter().getValueClass() == Timestamp.class) {
					final Date date = new SimpleDateFormat(Constants.DATETIME_TWO_DIGIT_YEAR_FORMAT).parse(newValue);
					setValue(new Timestamp(date.getTime()));
				} else if (getJrParameter().getValueClass() == Time.class) {
					final Date date = new SimpleDateFormat(Constants.DATETIME_TWO_DIGIT_YEAR_FORMAT).parse(newValue);
					setValue(new Time(date.getTime()));
				} else if (getJrParameter().getValueClass() == Double.class) {
					setValue(Double.parseDouble(newValue));
				} else if (getJrParameter().getValueClass() == Float.class) {
					setValue(Float.parseFloat(newValue));
				} else if (getJrParameter().getValueClass() == Boolean.class) {
					setValue(Boolean.parseBoolean(newValue));
				} else {
					throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
				}
			}
		} catch (final Exception e) {
			Log.REPORT.error("Invalid parameter value \"" + newValue + "\" for \"" + getJrParameter().getValueClass()
					+ "\"", e);
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_VALUE.createException();
		}
	}

}
