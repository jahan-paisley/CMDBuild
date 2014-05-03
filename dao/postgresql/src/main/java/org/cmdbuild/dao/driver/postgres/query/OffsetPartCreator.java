package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;

import org.cmdbuild.dao.query.QuerySpecs;

public class OffsetPartCreator extends PartCreator {

	private static final String OFFSET = "OFFSET";
	private static final String FORMAT = OFFSET + " %s";

	public OffsetPartCreator(final QuerySpecs querySpecs) {
		final Long offsetValue = querySpecs.getOffset();
		final String offset = (offsetValue == null) ? Long.toString(0L) : querySpecs.getOffset().toString();
		sb.append(format(FORMAT, offset));
	}

}
