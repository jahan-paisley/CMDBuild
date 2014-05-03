package org.cmdbuild.data.converter;

import static java.lang.String.format;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.widget.Widget;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class WidgetConverter extends BaseStorableConverter<Widget> {

	private static final Marker marker = MarkerFactory.getMarker(WidgetConverter.class.getName());

	private static final String WIDGETS_CLASSNAME = "_Widget";
	private static final String CODE_ATTRIBUTE = "Code";
	private static final String DESCRIPTION_ATTRIBUTE = "Description";
	private static final String DEFINITION_ATTRIBUTE = "Definition";

	@Override
	public Widget convert(final CMCard card) {
		Widget widget = null;
		try {
			widget = mapper().readValue(card.get(DEFINITION_ATTRIBUTE, String.class), new TypeReference<Widget>() {
			});
			widget.setSourceClass(card.get(CODE_ATTRIBUTE, String.class));
			widget.setId(card.getId());
		} catch (final Exception e) {
			logger.error(marker, "error converting widget", e);
		}
		return widget;
	}

	@Override
	public Map<String, Object> getValues(final Widget widget) {
		final Map<String, Object> result = Maps.newHashMap();
		result.put(CODE_ATTRIBUTE, widget.getSourceClass());
		result.put(DESCRIPTION_ATTRIBUTE, widget.getType());
		try {
			result.put(DEFINITION_ATTRIBUTE, mapper().writeValueAsString(widget));
		} catch (final Exception e) {
			logger.error(marker, format("error getting attribute '{}'", DEFINITION_ATTRIBUTE), e);
		}
		return result;
	}

	private ObjectMapper mapper() {
		return new ObjectMapper();
	}

	@Override
	public String getClassName() {
		return WIDGETS_CLASSNAME;
	}

}
