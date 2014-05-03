package org.cmdbuild.dao.driver.postgres.quote;

import static com.google.common.base.CharMatcher.inRange;

public class IdentQuoter implements Quoter {

	public static final String quote(final String name) {
		if (inRange('a', 'z').matchesAllOf(name)) {
			return name;
		} else {
			return String.format("\"%s\"", name.replace("\"", "\"\""));
		}
	}

	private final String name;

	public IdentQuoter(final String name) {
		this.name = name;
	}

	@Override
	public String quote() {
		return quote(name);
	}

}
