package org.cmdbuild.dao.query.clause.alias;

public class Utils {

	private Utils() {
		// prevents instantiation
	}

	/**
	 * Syntactic sugar.
	 * 
	 * @param alias
	 * 
	 * @return returns the same input parameter.
	 */
	public static Alias as(final Alias alias) {
		return alias;
	}

}
