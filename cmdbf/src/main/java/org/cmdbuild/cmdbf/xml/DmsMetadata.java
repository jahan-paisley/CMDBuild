package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dms.Metadata;

public class DmsMetadata implements Metadata {

	private final String name;
	private final String value;

	public DmsMetadata(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

}
