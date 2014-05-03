package org.cmdbuild.dao.constants;

public enum Cardinality {

	CARDINALITY_11("1:1"), //
	CARDINALITY_1N("1:N"), //
	CARDINALITY_N1("N:1"), //
	CARDINALITY_NN("N:N");

	private final String toString;

	private Cardinality(final String toString) {
		this.toString = toString;
	}

	public String value() {
		return toString;
	}

}
