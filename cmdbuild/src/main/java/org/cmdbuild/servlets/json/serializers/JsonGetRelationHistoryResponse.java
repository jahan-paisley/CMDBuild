package org.cmdbuild.servlets.json.serializers;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGetRelationHistoryResponse extends AbstractJsonResponseSerializer implements JsonSerializable {

	private final GetRelationHistoryResponse inner;

	public JsonGetRelationHistoryResponse(final GetRelationHistoryResponse inner) {
		this.inner = inner;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject jsonResponse;
		final JSONArray relationHistoryArray = relationHistoryToJson();
		jsonResponse = new JSONObject();
		jsonResponse.put("rows", relationHistoryArray);
		return jsonResponse;
	}

	private JSONArray relationHistoryToJson() throws JSONException {
		final JsonRelationHistoryFormatter formatter = new JsonRelationHistoryFormatter();
		for (final RelationInfo ri : inner) {
			formatter.addRelation(ri);
		}
		return formatter.toJson();
	}

	// FIXME It's an awful legacy way of serializing the relation
	private class JsonRelationHistoryFormatter extends JsonHistory {

		public JsonRelationHistoryFormatter() {
			super(null);
		}

		public void addRelation(final RelationInfo ri) {
			final CMRelation relation = ri.getRelation();
			addHistoryItem(new HistoryItem() {

				@Override
				public Long getId() {
					return relation.getId();
				}

				@Override
				public long getInstant() {
					return relation.getBeginDate().getMillis();
				}

				@Override
				public Map<String, ValueAndDescription> getAttributes() {
					final Map<String, ValueAndDescription> map = new HashMap<String, ValueAndDescription>();
					for (final CMAttribute attr : relation.getType().getActiveAttributes()) {
						try {
							final String name = attr.getName();
							final String description = attr.getDescription();
							final Object value = javaToJsonValue(attr.getType(), relation.get(name));
							map.put(name, new ValueAndDescription(value, description));
						} catch (final JSONException e) {
							// Skip attribute
						}
					}
					return map;
				}

				@Override
				public Map<String, Object> getExtraAttributes() {
					final Map<String, Object> map = new HashMap<String, Object>();
					map.put("_RelHist", true);
					map.put("DomainDesc", relation.getType().getDescription());
					map.put("User", relation.getUser());
					map.put("BeginDate", formatDateTime(relation.getBeginDate()));
					map.put("EndDate", formatDateTime(relation.getEndDate()));
					map.put("Class", ri.getTargetType().getName());
					map.put("CardCode", ri.getTargetCode());
					map.put("CardDescription", ri.getTargetDescription());
					return map;
				}

				@Override
				public boolean isInOutput() {
					// Skip active relations
					return (relation.getEndDate() != null);
				}

			});
		}
	}
}
