package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readDateTime;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.StorableProject;

public class StorableProjectConverter extends BaseStorableConverter<StorableProject> {

	public static final String TABLE_NAME = "_BimProject";
	public static final String PROJECT_ID = "ProjectId";

	final String NAME = "Code", DESCRIPTION = "Description", ACTIVE = "Active",
			LAST_CHECKIN = "LastCheckin", SYNCHRONIZED = "Synchronized", IMPORT_MAPPING = "ImportMapping",
			EXPORT_MAPPING = "ExportMapping", EXPORT_PROJECT_ID = "ExportProjectId",
			SHAPE_PROJECT_ID = "ShapesProjectId";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return PROJECT_ID;
	}

	@Override
	public StorableProject convert(CMCard card) {
		final StorableProject project = new StorableProject();
		project.setCardId(card.getId());
		project.setName(readString(card, NAME));
		project.setDescription(readString(card, DESCRIPTION));
		project.setProjectId(readString(card, PROJECT_ID));
		project.setActive(readBoolean(card, ACTIVE));
		project.setLastCheckin(readDateTime(card, LAST_CHECKIN));
		project.setSynch(readBoolean(card, SYNCHRONIZED));
		project.setImportMapping(readString(card, IMPORT_MAPPING));
		project.setExportMapping(readString(card, EXPORT_MAPPING));
		project.setExportProjectId(readString(card, EXPORT_PROJECT_ID));
		project.setShapeProjectId(readString(card, SHAPE_PROJECT_ID));
		return project;
	}

	@Override
	public Map<String, Object> getValues(StorableProject storableProject) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(NAME, storableProject.getName());
		values.put(DESCRIPTION, storableProject.getDescription());
		values.put(PROJECT_ID, storableProject.getProjectId());
		values.put(ACTIVE, storableProject.isActive());
		values.put(LAST_CHECKIN, storableProject.getLastCheckin());
		values.put(SYNCHRONIZED, storableProject.isSynch());
		values.put(EXPORT_PROJECT_ID, storableProject.getExportProjectId());
		// values.put(IMPORT_MAPPING, storableProject.getImportMapping());
		// values.put(EXPORT_MAPPING, storableProject.getExportMapping());
		// values.put(SHAPE_PROJECT_ID, storableProject.getShapeProjectId());
		return values;
	}

	@Override
	public String getUser(StorableProject storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
