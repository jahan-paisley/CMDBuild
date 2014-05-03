package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;

public interface CardDiffer {

	CMCard updateCard(Entity sourceEntity, CMCard oldCard);

	CMCard createCard(Entity sourceEntity);

}