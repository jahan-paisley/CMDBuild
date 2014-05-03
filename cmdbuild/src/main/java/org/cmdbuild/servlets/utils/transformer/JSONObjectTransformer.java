package org.cmdbuild.servlets.utils.transformer;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class JSONObjectTransformer extends AbstractTransformer<JSONObject> {

	public JSONObject transform(HttpServletRequest request, Object context,
			String... value) throws Exception {
		return new JSONObject(value[0]);
	}

}
