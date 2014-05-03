package org.cmdbuild.services.event;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface Command {

	Logger logger = Log.PERSISTENCE;

	void execute(Context context);

}
