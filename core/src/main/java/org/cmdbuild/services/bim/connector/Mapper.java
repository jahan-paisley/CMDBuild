package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.model.Entity;


public interface Mapper {

	void update(Iterable<Entity> source);

}