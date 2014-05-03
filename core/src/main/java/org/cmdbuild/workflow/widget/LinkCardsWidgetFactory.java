package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class LinkCardsWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "linkCards";

	public static final String FILTER = "Filter";
	public static final String CLASS_NAME = "ClassName";
	public static final String DEFAULT_SELECTION = "DefaultSelection";
	public static final String READ_ONLY = "NoSelect";
	public static final String SINGLE_SELECT = "SingleSelect";
	private static final String ALLOW_CARD_EDITING = "AllowCardEditing";
	private static final String WITH_MAP = "Map";
	private static final String MAP_LATITUDE = "StartMapWithLatitude";
	private static final String MAP_LONGITUDE = "StartMapWithLongitude";
	private static final String MAP_ZOOM = "StartMapWithZoom";
	public static final String REQUIRED = "Required";

	public LinkCardsWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final LinkCards widget = new LinkCards();

		setFilterAndClassName(valueMap, widget);
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setDefaultSelection(readString(valueMap.get(DEFAULT_SELECTION)));
		widget.setReadOnly(readBooleanTrueIfPresent(valueMap.get(READ_ONLY)));
		widget.setSingleSelect(readBooleanTrueIfPresent(valueMap.get(SINGLE_SELECT)));
		widget.setAllowCardEditing(readBooleanTrueIfPresent(valueMap.get(ALLOW_CARD_EDITING)));
		widget.setEnableMap(readBooleanTrueIfPresent(valueMap.get(WITH_MAP)));
		widget.setMapLatitude(readInteger(valueMap.get(MAP_LATITUDE)));
		widget.setMapLongitude(readInteger(valueMap.get(MAP_LONGITUDE)));
		widget.setMapZoom(readInteger(valueMap.get(MAP_ZOOM)));
		widget.setRequired(readBooleanTrueIfPresent(valueMap.get(REQUIRED)));
		widget.setTemplates(extractUnmanagedStringParameters(valueMap, FILTER, CLASS_NAME, DEFAULT_SELECTION,
				READ_ONLY, SINGLE_SELECT, ALLOW_CARD_EDITING, WITH_MAP, MAP_LATITUDE, MAP_LONGITUDE, MAP_ZOOM,
				REQUIRED, BUTTON_LABEL));

		return widget;
	}

	/*
	 * If the filter is set the given ClassName is ignored and is used the
	 * filter
	 */
	private void setFilterAndClassName(final Map<String, Object> valueMap, final LinkCards widget) {
		final String filter = readString(valueMap.get(FILTER));
		if (filter != null) {
			widget.setFilter(filter);
			widget.setClassName(readClassNameFromCQLFilter(filter));
		} else {
			widget.setClassName(readString(valueMap.get(CLASS_NAME)));
		}
	}

}
