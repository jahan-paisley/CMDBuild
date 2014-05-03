package org.cmdbuild.services.soap.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.common.Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeSerializer {

	private final String stringValue;
	private final DateTimeFormatter[] formatters;

	public DateTimeSerializer(String stringValue) {
		this.stringValue = stringValue;
		formatters = new DateTimeFormatter[] { DateTimeFormat.forPattern(Constants.DATE_FOUR_DIGIT_YEAR_FORMAT), //
				DateTimeFormat.forPattern(Constants.TIME_FORMAT), //
				DateTimeFormat.forPattern(Constants.DATETIME_FOUR_DIGIT_YEAR_FORMAT) };
	}

	public DateTime getValue() {
		for (DateTimeFormatter formatter : formatters) {
			try {
				return formatter.parseDateTime(stringValue);
			} catch (Exception ex) {
				// empty
			}
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.SOAP_ALL_DATES_FORMAT);
			Date date = sdf.parse(stringValue);
			return new DateTime(date);
		} catch (Exception ex) {
			// empty
		}
		return null;
	}

}
