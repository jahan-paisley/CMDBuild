package org.cmdbuild.servlets.utils.transformer;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

public class JSONArrayTransformer extends AbstractTransformer<JSONArray> {

	public JSONArray transform(HttpServletRequest request, Object context,
			String... value) throws Exception {
		return new JSONArray(value[0]);
	}

}
