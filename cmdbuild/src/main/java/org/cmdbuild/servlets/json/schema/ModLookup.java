package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.CODE_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.DEFAULT;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.LOOKUP_LIST;
import static org.cmdbuild.servlets.json.ComunicationConstants.NOTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.NUMBER;
import static org.cmdbuild.servlets.json.ComunicationConstants.ORIG_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.PARENT;
import static org.cmdbuild.servlets.json.ComunicationConstants.PARENT_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SHORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE_CAPITAL;

import java.util.Map;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.LookupSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class ModLookup extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONArray tree() throws JSONException {
		final Iterable<LookupType> elements = lookupLogic().getAllTypes();

		final JSONArray jsonLookupTypes = new JSONArray();
		for (final LookupType element : elements) {
			jsonLookupTypes.put(LookupSerializer.serializeLookupTable(element));
		}

		return jsonLookupTypes;
	}

	@JSONExported
	@Admin
	public JSONObject saveLookupType( //
			final JSONObject serializer, //
			final @Parameter(DESCRIPTION) String type, //
			final @Parameter(ORIG_TYPE) String originalType, //
			final @Parameter(value = PARENT, required = false) String parentType //
	) throws JSONException {
		final LookupType newType = LookupType.newInstance().withName(type).withParent(parentType).build();
		final LookupType oldType = LookupType.newInstance().withName(originalType).withParent(parentType).build();
		lookupLogic().saveLookupType(newType, oldType);

		final JSONObject jsonLookupType = LookupSerializer.serializeLookupTable(newType);
		serializer.put("lookup", jsonLookupType);
		if (isNotEmpty(originalType)) {
			jsonLookupType.put("oldId", originalType);
		} else {
			serializer.put("isNew", true);
		}

		return serializer;
	}

	@JSONExported
	public JSONObject getLookupList( //
			final JSONObject serializer, //
			final @Parameter(TYPE) String type, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = SHORT, required = false) boolean shortForm) //
			throws JSONException {

		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<Lookup> elements = lookupLogic().getAllLookup(lookupType, active);

		for (final Lookup element : elements) {
			serializer.append("rows", LookupSerializer.serializeLookup(element, shortForm));
		}

		serializer.put("total", size(elements));
		return serializer;
	}

	@JSONExported
	public JSONObject getParentList( //
			final @Parameter(value = TYPE, required = false) String type //
	) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<Lookup> elements = lookupLogic().getAllLookupOfParent(lookupType);

		for (final Lookup lookup : elements) {
			out.append("rows", LookupSerializer.serializeLookupParent(lookup));
		}

		return out;
	}

	@JSONExported
	@Admin
	public void disableLookup( //
			@Parameter(ID) final int id //
	) throws JSONException {
		lookupLogic().disableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public void enableLookup( //
			@Parameter(ID) final int id //
	) throws JSONException {
		lookupLogic().enableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public JSONObject saveLookup( //
			final JSONObject serializer, //
			final @Parameter(TYPE_CAPITAL) String type, //
			final @Parameter(CODE_CAPITAL) String code, //
			final @Parameter(DESCRIPTION_CAPITAL) String description, //
			final @Parameter(ID_CAPITAL) int id, //
			final @Parameter(PARENT_ID) int parentId, //
			final @Parameter(NOTES) String notes, //
			final @Parameter(DEFAULT) boolean isDefault, //
			final @Parameter(ACTIVE_CAPITAL) boolean isActive, //
			final @Parameter(NUMBER) int number //
	) throws JSONException {
		final Lookup lookup = Lookup.newInstance() //
				.withId(Long.valueOf(id)) //
				.withCode(code) //
				.withDescription(description) //
				.withType(LookupType.newInstance() //
				.withName(type)) //
				.withParentId(Long.valueOf(parentId)) //
				.withNotes(notes) //
				.withDefaultStatus(isDefault) //
				.withActiveStatus(isActive) //
				.build();

		final Long lookupId = lookupLogic().createOrUpdateLookup(lookup);
		lookup.setId(lookupId);

		serializer.put("lookup", LookupSerializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void reorderLookup( //
			final @Parameter(TYPE) String type, //
			final @Parameter(LOOKUP_LIST) JSONArray jsonPositions //
	) throws JSONException, AuthException {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(type) //
				.build();
		final Map<Long, Integer> positions = Maps.newHashMap();
		for (int i = 0; i < jsonPositions.length(); i++) {
			final JSONObject jsonElement = jsonPositions.getJSONObject(i);
			positions.put( //
					Long.valueOf(jsonElement.getInt("id")), //
					jsonElement.getInt("index"));
		}
		lookupLogic().reorderLookup(lookupType, positions);
	}

}
