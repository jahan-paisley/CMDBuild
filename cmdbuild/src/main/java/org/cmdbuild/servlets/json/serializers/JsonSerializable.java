package org.cmdbuild.servlets.json.serializers;

import org.json.JSONException;
import org.json.JSONObject;

interface JsonSerializable {

	JSONObject toJson() throws JSONException;
}
