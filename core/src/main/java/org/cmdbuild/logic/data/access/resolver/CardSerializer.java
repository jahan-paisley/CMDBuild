package org.cmdbuild.logic.data.access.resolver;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class CardSerializer<T extends CMEntry> extends ReferenceAndLookupSerializer<T> {

	@Override
	public void visit(final DateAttributeType attributeType) {
		final DateTime date = attributeType.convertValue(rawValue);
		final DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_PRINTING_PATTERN);

		setAttribute(attributeName, fmt.print(date));
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		final DateTime date = attributeType.convertValue(rawValue);
		final DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.TIME_PRINTING_PATTERN);

		setAttribute(attributeName, fmt.print(date));
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		final DateTime date = attributeType.convertValue(rawValue);
		final DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATETIME_PRINTING_PATTERN);

		setAttribute(attributeName, fmt.print(date));
	}

}
