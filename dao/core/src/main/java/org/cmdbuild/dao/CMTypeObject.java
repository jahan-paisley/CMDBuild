package org.cmdbuild.dao;

import org.cmdbuild.dao.entrytype.CMIdentifier;

public interface CMTypeObject {

	CMIdentifier getIdentifier();

	Long getId();

	/**
	 * @deprecated use {@link #getIdentifier()} instead.
	 */
	@Deprecated
	String getName();

}
