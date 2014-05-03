package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.Collections2.filter;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.soap.structure.ActivitySchema;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.Workflow;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

import com.google.common.base.Predicate;

public class WorkflowLogicHelper implements SoapLogicHelper {

	private static final WorkflowWidgetSubmission[] EMPTY_WORKFLOW_WIDGETS_SUBMISSION = new WorkflowWidgetSubmission[] {};

	private final WorkflowLogic workflowLogic;
	private final SerializationStuff serializationUtils;
	private final CardAdapter cardAdapter;

	public WorkflowLogicHelper( //
			final WorkflowLogic workflowLogic, //
			final CMDataView view, //
			final MetadataStoreFactory metedataStoreFactory, //
			final CardAdapter cardAdapter //
	) {
		this.workflowLogic = workflowLogic;
		this.serializationUtils = new SerializationStuff(view, metedataStoreFactory);
		this.cardAdapter = cardAdapter;
	}

	public String getInstructions(final String className, final Integer cardId) {
		try {
			return safeInstructionsFor(activityFor(className, cardId));
		} catch (final CMWorkflowException e) {
			final String message = format("cannot get instructions for className '{}' and cardId '{}'", className,
					cardId);
			logger.warn(message);
			return EMPTY;
		}
	}

	private String safeInstructionsFor(final CMActivity activity) {
		return (activity == null) ? EMPTY : activity.getInstructions();
	}

	public ActivitySchema getActivitySchema(final String className, final Integer cardId) {
		try {
			final ActivitySchema activitySchema = new ActivitySchema();
			activitySchema.setAttributes(activityAttributesFor(className, cardId));
			activitySchema.setWidgets(workflowWidgetDefinitionsFor(className, cardId));
			return activitySchema;
		} catch (final CMWorkflowException e) {
			final String message = format("cannot get activity schema for className '%s' and cardId '%d'", className,
					cardId);
			logger.error(message, e);
			forwardException(message, e);
			return null; // unreachable code
		}
	}

	public List<AttributeSchema> getAttributeSchemaList(final String className, final Integer cardId) {
		try {
			return activityAttributesFor(className, cardId);
		} catch (final CMWorkflowException e) {
			final String message = format("cannot get attribute schema object for className '%s' and cardId '%d'",
					className, cardId);
			logger.error(message, e);
			forwardException(message, e);
			return null; // unreachable code
		}
	}

	private CMActivity startActivityFor(final String className) throws CMWorkflowException {
		return activityFor(className, null);
	}

	private CMActivity activityFor(final String className, final Integer cardId) throws CMWorkflowException {
		final CMActivity activity;
		if (isStartActivity(cardId)) {
			activity = workflowLogic.getStartActivity(className);
		} else {
			final UserProcessInstance processInstance = workflowLogic.getProcessInstance(className, cardId.longValue());
			activity = selectActivityFor(processInstance);
		}
		return activity;
	}

	private CMActivity selectActivityFor(final UserProcessInstance processInstance) throws CMWorkflowException {
		return selectActivityInstanceFor(processInstance).getDefinition();
	}

	public UserActivityInstance selectActivityInstanceFor(final UserProcessInstance processInstance)
			throws CMWorkflowException {
		UserActivityInstance selectedActivityInstance = null;
		for (final UserActivityInstance activityInstance : processInstance.getActivities()) {
			if (selectedActivityInstance == null) {
				selectedActivityInstance = activityInstance;
			} else if (isLower(selectedActivityInstance, activityInstance)) {
				selectedActivityInstance = activityInstance;
			}
		}
		return selectedActivityInstance;
	}

	private boolean isLower(final UserActivityInstance activityInstance1, final UserActivityInstance activityInstance2) {
		return activityInstance1.getId().compareTo(activityInstance2.getId()) < 0;
	}

	private List<AttributeSchema> activityAttributesFor(final String className, final Integer cardId)
			throws CMWorkflowException {
		final CMActivity activity = activityFor(className, cardId);
		final List<AttributeSchema> attributeSchemas = new ArrayList<AttributeSchema>();
		int index = 0;
		final UserProcessClass userProcessClass = workflowLogic.findProcessClass(className);
		for (final CMActivityVariableToProcess variable : activity.getVariables()) {
			final CMAttribute attribute = userProcessClass.getAttribute(variable.getName());
			final AttributeSchema attributeSchema = serializationUtils.serialize(attribute, index++);
			attributeSchema.setVisibility(visibilityFor(variable));
			attributeSchemas.add(attributeSchema);
		}
		return attributeSchemas;
	}

	private String visibilityFor(final CMActivityVariableToProcess variable) {
		switch (variable.getType()) {
		case READ_ONLY:
			return "VIEW";
		case READ_WRITE:
			return "UPDATE";
		case READ_WRITE_REQUIRED:
			return "REQUIRED";
		default:
			throw new IllegalArgumentException("missing type mapping");
		}
	}

	private List<WorkflowWidgetDefinition> workflowWidgetDefinitionsFor(final String className, final Integer cardId)
			throws CMWorkflowException {
		final List<WorkflowWidgetDefinition> widgetList = new ArrayList<WorkflowWidgetDefinition>();
		final CMActivity activity = activityFor(className, cardId);
		for (final CMActivityWidget widget : activity.getWidgets()) {
			final Widget concreteWidget = Widget.class.cast(widget);
			final SoapWidgetSerializer serializer = new SoapWidgetSerializer(concreteWidget);
			final WorkflowWidgetDefinition wwd = serializer.serialize();
			widgetList.add(wwd);
		}
		return widgetList;
	}

	public Workflow updateProcess(final Card card, final boolean advance) {
		return updateProcess(card, EMPTY_WORKFLOW_WIDGETS_SUBMISSION, advance);
	}

	public Workflow updateProcess(final Card card, final WorkflowWidgetSubmission[] widgets, final boolean advance) {
		try {
			cardAdapter.resolveAttributes(card);
			final UserProcessInstance processInstance;
			if (isNewProcess(card)) {
				final CMActivity activity = startActivityFor(card.getClassName());
				final List<CMActivityWidget> activityWidgets = activity.getWidgets();
				processInstance = workflowLogic.startProcess( //
						card.getClassName(), //
						variablesFor(card), //
						widgetSubmission(activityWidgets, widgets), //
						advance);
			} else {
				processInstance = workflowLogic.getProcessInstance( //
						card.getClassName(), //
						longIdFor(card));
				final UserActivityInstance selectedActivity = selectActivityInstanceFor(processInstance);
				final List<CMActivityWidget> activityWidgets = selectedActivity.getWidgets();
				workflowLogic.updateProcess( //
						card.getClassName(), //
						longIdFor(card), //
						selectedActivity.getId(), //
						variablesFor(card), //
						widgetSubmission(activityWidgets, widgets), //
						advance);
			}
			return workflowFor(processInstance);
		} catch (final CMWorkflowException e) {
			final String message = format("cannot update process for className '%s' and cardId '%d'",
					card.getClassName(), card.getId());
			logger.error(message, e);
			forwardException(message, e);
			return null; // unreachable code
		}
	}

	private boolean isNewProcess(final Card card) {
		return isStartActivity(card.getId());
	}

	private boolean isStartActivity(final Integer cardId) {
		return (cardId == null) ? true : (cardId <= 0);
	}

	private long longIdFor(final Card card) {
		return new Integer(card.getId()).longValue();
	}

	private Map<String, Object> variablesFor(final Card card) {
		final Map<String, Object> variables = new HashMap<String, Object>();
		for (final Attribute attribute : safeAttributesFor(card)) {
			variables.put(attribute.getName(), attribute.getValue());
		}
		return variables;
	}

	private List<Attribute> safeAttributesFor(final Card card) {
		List<Attribute> attributes = card.getAttributeList();
		if (attributes == null) {
			attributes = Collections.emptyList();
		}
		return attributes;
	}

	private Map<String, Object> widgetSubmission(final List<CMActivityWidget> activityWidgets,
			final WorkflowWidgetSubmission[] widgets) {
		final Map<String, Object> widgetSubmissions = new HashMap<String, Object>();
		for (final WorkflowWidgetSubmission submission : safeWidgetListOf(widgets)) {
			final String widgetId = submission.getIdentifier();
			final Collection<CMActivityWidget> filteredActivityWidgets = filter(activityWidgets,
					widgetIdEqualsTo(widgetId));
			if (!filteredActivityWidgets.isEmpty()) {
				final CMActivityWidget activityWidget = filteredActivityWidgets.iterator().next();
				final Widget widget = Widget.class.cast(activityWidget);
				final WidgetSubmissionConverter converter = new WidgetSubmissionConverter(widget);
				final Object widgetSubmission = converter.convertFrom(submission);
				if (widgetSubmission != null) {
					widgetSubmissions.put(widgetId, widgetSubmission);
				}
			}
		}
		return widgetSubmissions;
	}

	private Predicate<CMActivityWidget> widgetIdEqualsTo(final String widgetId) {
		return new Predicate<CMActivityWidget>() {
			@Override
			public boolean apply(final CMActivityWidget input) {
				return input.getStringId().equals(widgetId);
			}
		};
	}

	private List<WorkflowWidgetSubmission> safeWidgetListOf(final WorkflowWidgetSubmission[] widgets) {
		final List<WorkflowWidgetSubmission> list;
		if (widgets == null) {
			list = Collections.emptyList();
		} else {
			list = Arrays.asList(widgets);
		}
		return list;
	}

	private Workflow workflowFor(final UserProcessInstance processInstance) {
		final Workflow workflow = new Workflow();
		workflow.setProcessid(processInstance.getCardId().intValue());
		workflow.setProcessinstanceid(processInstance.getProcessInstanceId());
		return workflow;
	}

	private void forwardException(final String message, final CMWorkflowException e) {
		throw new RuntimeException(message, e);
	}

	public void resumeProcess(final Card card) throws CMWorkflowException {
		try {
			workflowLogic.resumeProcess(card.getClassName(), longIdFor(card));
		} catch (final CMWorkflowException e) {
			final String message = format("cannot resume process for className '%s' and cardId '%d'",
					card.getClassName(), card.getId());
			logger.error(message, e);
			forwardException(message, e);
		}
	}

}
