package org.cmdbuild.servlets.json.legacy;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public abstract class GraphItem {

	public abstract Element toXMLElement();
	
	protected Element serializeData(String key, String value) {
		Element data = DocumentHelper.createElement("data");
		data.addAttribute("key", key);
		if (value != null)
			data.addText(value);
		return data;
	}
}
