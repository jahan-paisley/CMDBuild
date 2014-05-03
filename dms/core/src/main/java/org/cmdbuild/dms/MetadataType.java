package org.cmdbuild.dms;

public enum MetadataType {

	TEXT("TEXT"), //
	INTEGER("INTEGER"), //
	FLOAT("DOUBLE"), //
	DATE("DATE"), //
	DATETIME("TIMESTAMP"), //
	BOOLEAN("BOOLEAN"), //
	LIST("LIST"), //
	;

	private final String id;

	private MetadataType(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
