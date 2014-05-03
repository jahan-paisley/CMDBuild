package org.cmdbuild.services.template.store;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.collect.Maps;

public class TemplateStorableConverter extends BaseStorableConverter<Template> {

	private static final String TEMPLATES_TABLE = "_Templates";
	private static final String TEMPLATE_NAME = "Name";
	private static final String TEMPLATE_DEFINITION = "Template";

	@Override
	public String getClassName() {
		return TEMPLATES_TABLE;
	}

	@Override
	public String getIdentifierAttributeName() {
		return TEMPLATE_NAME;
	}

	@Override
	public Template convert(final CMCard card) {
		final String key = card.get(TEMPLATE_NAME, String.class);
		final String value = card.get(TEMPLATE_DEFINITION, String.class);
		return Template.of(key, value);
	}

	@Override
	public Map<String, Object> getValues(final Template storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(TEMPLATE_NAME, storable.getKey());
		values.put(TEMPLATE_DEFINITION, storable.getValue());
		return values;
	}

}
