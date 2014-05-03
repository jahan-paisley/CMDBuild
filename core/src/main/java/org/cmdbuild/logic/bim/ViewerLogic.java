package org.cmdbuild.logic.bim;

import java.util.List;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.services.bim.DefaultBimDataView.BimCard;

public interface ViewerLogic extends Logic {

	BimCard fetchCardDataFromObjectId(String objectId, String revisionId);

	String getDescriptionOfRoot(Long cardId, String className);

	String getBaseRevisionIdForViewer(Long cardId, String className);

	String getExportedRevisionIdForViewer(Long cardId, String className);

	String getBaseProjectId(Long cardId, String className);

	String getOutputForBimViewer(String revisionId, String baseProjectId);

	void moveObjectToPosition(String projectId, String className, String globalId, List<Double> coordinates);

}