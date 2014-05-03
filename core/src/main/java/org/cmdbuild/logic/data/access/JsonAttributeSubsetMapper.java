package org.cmdbuild.logic.data.access;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;

import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.logic.Logic;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.common.collect.Lists;

public class JsonAttributeSubsetMapper implements Mapper<JSONArray, List<QueryAliasAttribute>> {

	private final CMEntryType entryType;

	public JsonAttributeSubsetMapper(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	@Override
	public List<QueryAliasAttribute> map(final JSONArray jsonAttributes) {
		if (jsonAttributes.length() == 0) {
			return Lists.newArrayList();
		}
		final List<QueryAliasAttribute> attributeSubset = Lists.newArrayList();
		for (int i = 0; i < jsonAttributes.length(); i++) {
			try {
				final String attributeName = jsonAttributes.getString(i);
				if (entryType.getAttribute(attributeName) != null) {
					final QueryAliasAttribute attr = attribute(entryType, attributeName);
					attributeSubset.add(attr);
				}
			} catch (final JSONException ex) {
				Logic.logger.error("Cannot read attribute...");
			}
		}
		return attributeSubset;
	}
}
