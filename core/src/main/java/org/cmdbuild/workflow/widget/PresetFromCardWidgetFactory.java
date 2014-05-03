package org.cmdbuild.workflow.widget;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.model.widget.PresetFromCard;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class PresetFromCardWidgetFactory extends ValuePairWidgetFactory {

	private static final String 	WIDGET_NAME = "presetFromCard",
									FILTER = "Filter",
									ATTRIBUTE_MAPPING = "AttributeMapping",
									CLASS_NAME = "ClassName",

									PRESET_SEPARATOR_CHAR = ",",
									PRESET_MAPPING_CHAR = "=";

	public PresetFromCardWidgetFactory(
			TemplateRepository templateRespository, //
			Notifier notifier //
		) {

		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(Map<String, Object> valueMap) {
		final PresetFromCard widget = new PresetFromCard();

		setFilterAndClassName(valueMap, widget);
		widget.setPresetMapping(readPresets(valueMap));
		widget.setTemplates( //
				extractUnmanagedStringParameters( //
						valueMap, FILTER, CLASS_NAME, //
						BUTTON_LABEL, ATTRIBUTE_MAPPING //
					) //
				);

		return widget;
	}

	/*
	 * The presets is a string with this form
	 * "nameActivityAttribute=nameCardAttribute, nameActivityAttribute=nameCardAttribute, ..."
	 */
	private Map<String, String> readPresets( //
			final Map<String, Object> valueMap //
		) {

		final Map<String, String> out = new HashMap<String, String>();
		final String mapping = readString(valueMap.get(ATTRIBUTE_MAPPING));
		if (mapping != null) {
			final String[] presets = mapping.split(PRESET_SEPARATOR_CHAR);
			for (int i=0, l=presets.length; i<l; ++i) {
				final String preset = presets[i];
				final String[] presetPart = preset.split(PRESET_MAPPING_CHAR);
				if (presetPart.length == 2) {
					final String activityAttributeName = presetPart[0].trim();
					final String cardAttributeName = presetPart[1].trim();

					out.put(activityAttributeName, cardAttributeName);
				}
			}
		}

		return out;
	}

	private void setFilterAndClassName( //
			final Map<String, Object> valueMap, //
			final PresetFromCard widget //
		) {

		final String filter = readString(valueMap.get(FILTER));
		if (filter != null) {
			widget.setFilter(filter);
			widget.setClassName(readClassNameFromCQLFilter(filter));
		} else {
			widget.setClassName(readString(valueMap.get(CLASS_NAME)));
		}
	}
}
