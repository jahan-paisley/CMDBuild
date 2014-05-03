package org.cmdbuild.services.bim.connector;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;

public interface BimOperationListener extends EventListener {

	void createBimCard(Entity source, CMCard card);
	
	void updateTarget(Entity source, CMCard target);

}
