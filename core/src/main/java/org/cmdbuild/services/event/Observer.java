package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface Observer {

	Logger logger = Log.PERSISTENCE;

	void afterCreate(CMCard current);

	void beforeUpdate(CMCard current, CMCard next);

	void afterUpdate(CMCard previous, CMCard current);

	void beforeDelete(CMCard current);

}