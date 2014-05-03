package org.cmdbuild.logic.bim;

import org.cmdbuild.logic.Logic;

public interface SynchronizationLogic extends Logic {

	void importIfc(String projectId);

	void exportIfc(String sourceProjectId);

}
