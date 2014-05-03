package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.widget.NavigationTree;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class NavigationTreeWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "navigationTree";

	public static final String NAVIGATION_TREE_NAME = "NavigationTreeName";
	public static final String NAVIGATION_TREE_FILTER_TYPE = "FilterType";
	public static final String NAVIGATION_TREE_FILTER = "Filter";
	private static final Object DESCRIPTION = "Description";

	//FILTER TYPES
	public static final String NAMEFILTERTYPE = "name";
	public static final String CQLFILTERTYPE = "cql";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, NAVIGATION_TREE_NAME };


	public NavigationTreeWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String filterType = readString(valueMap.get(NAVIGATION_TREE_FILTER_TYPE));
		if (filterType != null && CQLFILTERTYPE.equals(CQLFILTERTYPE) ) {
			/*TODO Control on filter types
			 * At the moment there is only one filter type and is 'cql'
			 */
			final String filter = readString(valueMap.get(NAVIGATION_TREE_FILTER));
			Validate.notEmpty(filter, NAVIGATION_TREE_FILTER + " is required");
			final NavigationTree widget = new NavigationTree();
			widget.setFilter(filter);
			return widget;
		}
		else {
			final String navigationTreeName = readString(valueMap.get(NAVIGATION_TREE_NAME));
			Validate.notEmpty(navigationTreeName, NAVIGATION_TREE_NAME + " is required");
			final NavigationTree widget = new NavigationTree();
			widget.setNavigationTreeName(navigationTreeName);
			widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
			widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
			widget.setDescription(readString(valueMap.get(DESCRIPTION)));
			return widget;
		}
	}

}
