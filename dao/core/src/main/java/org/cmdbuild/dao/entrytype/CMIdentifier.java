package org.cmdbuild.dao.entrytype;

public interface CMIdentifier {

	String DEFAULT_NAMESPACE = null;

	String getLocalName();

	String getNameSpace();

}
