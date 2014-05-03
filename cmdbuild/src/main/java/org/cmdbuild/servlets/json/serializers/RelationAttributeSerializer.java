package org.cmdbuild.servlets.json.serializers;

import java.util.Map;

import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class RelationAttributeSerializer {

	private final LookupStore lookupStore;

	public RelationAttributeSerializer(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public final JSONObject toClient( //
			final RelationInfo relationInfo //
	) throws JSONException {

		return toClient(relationInfo, false);
	}

	public final JSONObject toClient( //
			final RelationInfo relationInfo, //
			final boolean cardReferencesWithIdAndDescription //
	) throws JSONException {
		final JSONObject jsonAttributes = new JSONObject();
		final CMDomain domain = relationInfo.getRelation().getType();

		for (final Map.Entry<String, Object> attribute : relationInfo.getRelationAttributes()) {
			final CMAttributeType<?> attributeType = domain.getAttribute(attribute.getKey()).getType();
			final Object value = attribute.getValue();

			if (attributeType instanceof LookupAttributeType //
					&& value != null) { //

				final IdAndDescription cardReference = IdAndDescription.class.cast(value);
				Lookup lookup = null;
				if (cardReference.getId() != null) {
					lookup = lookupStore.read(createFakeStorableFrom((cardReference.getId())));
				}

				if (lookup != null) {
					attribute.setValue(new IdAndDescription(lookup.getId(), lookup.description));
				}
			}

			final JavaToJSONValueConverter valueConverter = new JavaToJSONValueConverter(attributeType, //
					attribute.getValue(), //
					cardReferencesWithIdAndDescription //
			);

			jsonAttributes.put( //
					attribute.getKey(), //
					valueConverter.valueForJson() //
					);
		}

		return jsonAttributes;
	}

	private static Storable createFakeStorableFrom(final Long storableId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return storableId.toString();
			}
		};
	}
}
