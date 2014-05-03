package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.Workflow;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class StartWorkflowWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "startWorkflow";

	public static final String WORKFLOW_CODE = "WorkflowCode";
	public static final String WORKFLOW_FILTER_TYPE = "FilterType";
	public static final String WORKFLOW_FILTER = "Filter";

	//FILTER TYPES
	public static final String NAMEFILTERTYPE = "name";
	public static final String CQLFILTERTYPE = "cql";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, WORKFLOW_CODE };

	public StartWorkflowWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String filterType = readString(valueMap.get(WORKFLOW_FILTER_TYPE));
		if (filterType != null && CQLFILTERTYPE.equals(CQLFILTERTYPE) ) {
			/*TODO Control on filter types
			 * At the moment there is only one filter type and is 'cql'
			 */
			final String filter = readString(valueMap.get(WORKFLOW_FILTER));
			Validate.notEmpty(filter, WORKFLOW_FILTER + " is required");
			final Workflow widget = new Workflow();
			widget.setFilterType(filterType);
			widget.setFilter(filter);
			widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
			return widget;
		}
		else {
			final String workflowCode = readString(valueMap.get(WORKFLOW_CODE));
			Validate.notEmpty(workflowCode, WORKFLOW_CODE + " is required");
			final Workflow widget = new Workflow();
			widget.setWorkflowName(workflowCode);
			widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
			widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
			return widget;
		}
	}

}
