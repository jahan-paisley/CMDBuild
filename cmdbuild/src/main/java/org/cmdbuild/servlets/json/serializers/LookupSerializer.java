package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	private final static LookupStore lookupStore = applicationContext().getBean(LookupStore.class);

	public static JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public static JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("Id", lookup.getId());
			serializer.put("Description", lookup.description);

			if (!shortForm) {
				serializer.put("Type", lookup.type.name);
				serializer.put("Code", defaultIfEmpty(lookup.code, EMPTY));
				serializer.put("Number", lookup.number);
				serializer.put("Notes", lookup.notes);
				serializer.put("Default", lookup.isDefault);
				serializer.put("Active", lookup.active);
			}

			final Lookup parent = lookup.parent;
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {
					serializer.put("ParentDescription", parent.description);
					serializer.put("ParentType", parent.type);
				}
			}
		}
		return serializer;
	}

	public static JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("ParentId", lookup.getId());
			serializer.put("ParentDescription", lookup.description);
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.name);
		serializer.put("text", lookupType.name);
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.parent != null) {
			serializer.put("parent", lookupType.parent);
		}
		return serializer;
	}

	public static Map<String, Object> serializeLookupValue( //
			final LookupValue value //
			) {

		final Map<String, Object> out = new HashMap<String, Object>();
		out.put(ID, value.getId());
		out.put(DESCRIPTION, description(value));

		return out;
	}

	private static String description(final LookupValue value) {
		String description = value.getDescription();
		if (value instanceof LookupValue) {
			Lookup lookup = lookup(value.getId());
			if (lookup != null) {
				lookup = lookup(lookup.parentId);
				while(lookup != null) {
					description = lookup.description + " - " + description;
					lookup = lookup(lookup.parentId);
				}
			}
		}

		return description;
	}

	private static Lookup lookup(final Long id) {
		if (id != null) {
			return lookupStore.read(new Storable() {
				@Override
				public String getIdentifier() {
					return id.toString();
				}
			});
		} else {
			return null;
		}
	}
}
