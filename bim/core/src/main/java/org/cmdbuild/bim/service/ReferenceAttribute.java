package org.cmdbuild.bim.service;

import org.cmdbuild.bim.model.Attribute;

public interface ReferenceAttribute extends Attribute {

	String getGlobalId();

	long getOid();

	String getTypeName();

}
