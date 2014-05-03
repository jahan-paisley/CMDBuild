package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.model.Report;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportSerializer {
	public static JSONObject toClient(final Report report) throws JSONException {
		JSONObject serializer = new JSONObject();
		serializer.put("id", report.getId());
		serializer.put("title", report.getCode());
		serializer.put("description", report.getDescription());
		serializer.put("type", report.getType());
		serializer.put("query", report.getQuery());
		serializer.put("groups", report.getGroups());

		return serializer;
	}
}
