package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.WIDGET;
import static org.cmdbuild.servlets.json.ComunicationConstants.WIDGET_ID;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.logic.widget.WidgetLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMActivityWidget;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModWidget extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse callWidget(@Parameter("id") final Long cardId, @Parameter("className") final String className,
			@Parameter(required = false, value = "activityId") final String activityInstanceId,
			@Parameter("widgetId") final String widgetId,
			@Parameter(required = false, value = "action") final String action,
			@Parameter(required = false, value = "params") final String jsonParams) throws Exception {
		final boolean isActivity = activityInstanceId != null;
		if (isActivity) {
			return callProcessWidget(cardId, className, activityInstanceId, widgetId, action, jsonParams);
		} else {
			return callCardWidget(cardId, className, Long.parseLong(widgetId), action, jsonParams);
		}
	}

	private JsonResponse callCardWidget(final Long cardId, final String className, final Long widgetId,
			final String action, final String jsonParams) throws Exception {
		final WidgetLogic widgetLogic = new WidgetLogic(systemDataView());
		final Widget widgetToExecute = widgetLogic.getWidget(widgetId);
		final Card card = systemDataAccessLogic().fetchCard(className, cardId);
		final Map<String, Object> params = readParams(jsonParams);
		final Map<String, Object> attributesNameToValue = Maps.newHashMap();
		for (final Entry<String, Object> entry : card.getAttributes().entrySet()) {
			attributesNameToValue.put(entry.getKey(), entry.getValue());
		}
		return JsonResponse.success(widgetToExecute.executeAction(action, params, attributesNameToValue));
	}

	private JsonResponse callProcessWidget(final Long processCardId, final String className,
			final String activityInstanceId, final String widgetId, final String action, final String jsonParams)
			throws Exception {

		final Map<String, Object> params = readParams(jsonParams);
		Object response = null;
		final WorkflowLogic logic = workflowLogic();
		final List<CMActivityWidget> widgets;
		if (processCardId > 0) {
			final CMActivityInstance activityInstance = logic.getActivityInstance(className, processCardId,
					activityInstanceId);
			widgets = activityInstance.getWidgets();
		} else {
			// For a new process, there isn't activity instances. So retrieve
			// the start activity
			// and look for them widgets
			final CMActivity activity = logic.getStartActivity(className);
			widgets = activity.getWidgets();
		}

		for (final CMActivityWidget widget : widgets) {
			if (widget.getStringId().equals(widgetId)) {
				/*
				 * TODO
				 * 
				 * I don't know WTF pass instead of null, something for the
				 * server side TemplateResolver
				 */
				response = widget.executeAction(action, params, null);
			}
		}

		return JsonResponse.success(response);
	}

	@JSONExported
	public JsonResponse getAllWidgets() {
		final WidgetLogic widgetLogic = new WidgetLogic(systemDataView());
		final List<Widget> fetchedWidgets = widgetLogic.getAllWidgets();
		final Map<String, List<Widget>> classNameToWidgetList = Maps.newHashMap();
		for (final Widget widget : fetchedWidgets) {
			List<Widget> widgetList;
			if (!classNameToWidgetList.containsKey(widget.getSourceClass())) {
				widgetList = Lists.newArrayList();
				classNameToWidgetList.put(widget.getSourceClass(), widgetList);
			} else {
				widgetList = classNameToWidgetList.get(widget.getSourceClass());
			}
			widgetList.add(widget);
		}
		return JsonResponse.success(classNameToWidgetList);
	}

	@Admin
	@JSONExported
	public JsonResponse saveWidgetDefinition(@Parameter(CLASS_NAME) final String className, //
			@Parameter(value = WIDGET, required = true) final String jsonWidget) throws Exception {
		final WidgetLogic widgetLogic = new WidgetLogic(systemDataView());
		final ObjectMapper mapper = new ObjectMapper();
		final Widget widgetToSave = mapper.readValue(jsonWidget, Widget.class);
		widgetToSave.setSourceClass(className);
		Widget responseWidget = widgetToSave;
		if (widgetToSave.getIdentifier() == null) {
			responseWidget = widgetLogic.createWidget(widgetToSave);
		} else {
			widgetLogic.updateWidget(widgetToSave);
		}
		return JsonResponse.success(responseWidget);
	}

	@Admin
	@JSONExported
	public void removeWidgetDefinition(@Parameter(CLASS_NAME) final String className, //
			@Parameter(WIDGET_ID) final Long widgetId) throws Exception {
		final WidgetLogic widgetLogic = new WidgetLogic(systemDataView());
		widgetLogic.deleteWidget(widgetId);
	}

	private Map<String, Object> readParams(final String jsonParams) throws IOException, JsonParseException,
			JsonMappingException {
		final Map<String, Object> params;

		if (jsonParams == null) {
			params = new HashMap<String, Object>();
		} else {
			final ObjectMapper mapper = new ObjectMapper();
			params = new ObjectMapper().readValue(jsonParams,
					mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
		}
		return params;
	}

}
