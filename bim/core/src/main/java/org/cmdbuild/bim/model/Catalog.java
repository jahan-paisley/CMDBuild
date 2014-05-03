package org.cmdbuild.bim.model;

import java.util.List;

public interface Catalog {

	EntityDefinition getEntityDefinition(int i);

	Iterable<EntityDefinition> getEntitiesDefinitions();
	
	String toString();

	int getSize();

	boolean contains(String entityDefintionName);

	List<Integer> getPositionsOf(String entityDefintionName);

}
