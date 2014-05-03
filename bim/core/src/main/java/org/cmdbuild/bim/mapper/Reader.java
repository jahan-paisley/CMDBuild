package org.cmdbuild.bim.mapper;

import java.util.EventListener;
import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;

public interface Reader {

	interface ReaderListener extends EventListener {

		void retrieved(Entity entity);

	}

	List<Entity> readEntities(String revisionId,
			EntityDefinition entityDefinition);

}
