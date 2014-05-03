package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.common.Constants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimeAttributeType extends AbstractDateAttributeType {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected DateTimeFormatter[] getFormatters() {
		return new DateTimeFormatter[] { DateTimeFormat.forPattern(Constants.TIME_PARSING_PATTERN),
				DateTimeFormat.forPattern(Constants.SOAP_ALL_DATES_PARSING_PATTERN) };
	}

}
