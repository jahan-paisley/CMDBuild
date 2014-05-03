package org.cmdbuild.bim.service;

import java.util.List;

import org.cmdbuild.bim.model.Attribute;

public interface ListAttribute extends Attribute {

	List<Attribute> getValues();

}
