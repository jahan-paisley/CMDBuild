package org.cmdbuild.services.bim.connector;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;

public interface ImportDifferListener extends EventListener {
	
	void createTarget(Entity source);
	
	void deleteTarget(CMCard target);
	
	void updateTarget(Entity source, CMCard target);

}
