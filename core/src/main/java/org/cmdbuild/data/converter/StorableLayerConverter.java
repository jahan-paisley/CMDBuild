package org.cmdbuild.data.converter;

import static org.cmdbuild.logic.data.Utils.readBoolean;
import static org.cmdbuild.logic.data.Utils.readString;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.bim.StorableLayer;

public class StorableLayerConverter extends BaseStorableConverter<StorableLayer> {

	final String	TABLE_NAME = "_BimLayer",
					CLASS_NAME = "ClassName",
					ACTIVE = "Active",
					BIM_ROOT = "Root",
					EXPORT = "Export",	
					CONTAINER = "Container",
					ROOT_REFERENCE = "RootReference";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return CLASS_NAME;
	}

	@Override
	public StorableLayer convert(CMCard card) {
		final StorableLayer layer = new StorableLayer(readString(card, CLASS_NAME));
		layer.setActive(readBoolean(card,ACTIVE));
		layer.setRoot(readBoolean(card, BIM_ROOT));
		layer.setExport(readBoolean(card, EXPORT));
		layer.setContainer(readBoolean(card, CONTAINER));
		layer.setRootReference(readString(card,ROOT_REFERENCE));
		return layer;
	}

	@Override
	public Map<String, Object> getValues(StorableLayer layer) {
		final Map<String, Object> values = new HashMap<String, Object>();

		values.put(CLASS_NAME, layer.getClassName());
		values.put(ACTIVE, layer.isActive());
		values.put(BIM_ROOT, layer.isRoot());
		values.put(EXPORT, layer.isExport());
		values.put(CONTAINER, layer.isContainer());
		values.put(ROOT_REFERENCE, layer.getRootReference());
		return values;
	}

	@Override
	public String getUser(StorableLayer storable) {
		// TODO Auto-generated method stub
		return null;
	}

}
