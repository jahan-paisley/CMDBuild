package org.cmdbuild.services.cache;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface CachingService {

	Logger logger = Log.CMDBUILD;

	interface Cacheable {

		Logger logger = CachingService.logger;

		void clearCache();

	}

	void clearCache();

}
