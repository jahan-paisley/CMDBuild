package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;

import org.cmdbuild.dao.query.QuerySpecs;

public class LimitPartCreator extends PartCreator {

	private static final String LIMIT = "LIMIT";
	private static final String ALL = "ALL";
	private static final String FORMAT = LIMIT + " %s";

	public LimitPartCreator(final QuerySpecs querySpecs) {
		final Long limitValue = querySpecs.getLimit();
		final String limit = (limitValue == null || limitValue == 0) ? ALL : querySpecs.getLimit().toString();
		sb.append(format(FORMAT, limit));
	}

}
