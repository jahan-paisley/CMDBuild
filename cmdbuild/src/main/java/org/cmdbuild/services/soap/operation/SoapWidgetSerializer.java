package org.cmdbuild.services.soap.operation;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.model.Report;
import org.cmdbuild.model.widget.Calendar;
import org.cmdbuild.model.widget.CreateModifyCard;
import org.cmdbuild.model.widget.Grid;
import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.NavigationTree;
import org.cmdbuild.model.widget.OpenAttachment;
import org.cmdbuild.model.widget.OpenNote;
import org.cmdbuild.model.widget.OpenReport;
import org.cmdbuild.model.widget.Ping;
import org.cmdbuild.model.widget.PresetFromCard;
import org.cmdbuild.model.widget.WebService;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.model.widget.Workflow;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.soap.structure.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.structure.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.workflow.widget.CalendarWidgetFactory;
import org.cmdbuild.workflow.widget.CreateModifyCardWidgetFactory;
import org.cmdbuild.workflow.widget.LinkCardsWidgetFactory;
import org.cmdbuild.workflow.widget.OpenReportWidgetFactory;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;

class SoapWidgetSerializer implements WidgetVisitor {

	/**
	 * These constants are intended to be for legacy purpose only!
	 * 
	 * Only the constants defined in the {@link OpenReportWidgetFactory} implementations
	 * should be used.
	 */
	@Legacy("no comment")
	private static class LegacyConstants {

		public static final String ID = "Id";

		public static final String REPORT_TYPE = "ReportType";
		public static final String FORCE_EXTENSION = "forceextension";

		public static final ReportType DEFAULT_REPORT_TYPE = ReportType.CUSTOM;
		public static final String DEFAULT_REPORT_TYPE_AS_STRING = DEFAULT_REPORT_TYPE.name().toLowerCase();

	}

	private static class AdditionalConstants {

		public static final String OUTPUT_NAME = "outputName";

	}

	private final Widget widget;

	private final WorkflowWidgetDefinition definition;

	public SoapWidgetSerializer(final Widget widget) {
		this.widget = widget;
		this.definition = new WorkflowWidgetDefinition(widget.getType(), widget.getStringId());
	}

	public WorkflowWidgetDefinition serialize() {
		widget.accept(this);
		return definition;
	}

	@Override
	public void visit(final Calendar calendar) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, calendar.getLabel()));
		parameters.add(parameterFor(CalendarWidgetFactory.TARGET_CLASS, calendar.getSourceClass()));
		parameters.add(parameterFor(CalendarWidgetFactory.CQL_FILTER, calendar.getFilter()));
		parameters.add(parameterFor(CalendarWidgetFactory.TITLE, calendar.getEventTitle()));
		parameters.add(parameterFor(CalendarWidgetFactory.START_DATE, calendar.getStartDate()));
		parameters.add(parameterFor(CalendarWidgetFactory.END_DATE, calendar.getEndDate()));
		parameters.add(parameterFor(CalendarWidgetFactory.DEFAULT_DATE, calendar.getDefaultDate()));
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final CreateModifyCard createModifyCard) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, createModifyCard.getLabel()));
		parameters.add(parameterFor(CreateModifyCardWidgetFactory.CLASS_NAME, createModifyCard.getTargetClass()));
		parameters.add(parameterFor(CreateModifyCardWidgetFactory.OBJ_ID, createModifyCard.getIdcardcqlselector()));
		parameters.add(parameterFor(CreateModifyCardWidgetFactory.READONLY, createModifyCard.isReadonly()));
		parameters.add(parameterFor(LegacyConstants.ID, createModifyCard.getIdcardcqlselector()));
		parameters.add(parameterFor(AdditionalConstants.OUTPUT_NAME, createModifyCard.getOutputName()));
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final LinkCards linkCards) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, linkCards.getLabel()));
		parameters.add(parameterFor(LinkCardsWidgetFactory.CLASS_NAME, linkCards.getClassName()));
		parameters.add(parameterFor(LinkCardsWidgetFactory.SINGLE_SELECT, boolToInt(linkCards.isSingleSelect())));
		parameters.add(parameterFor(LinkCardsWidgetFactory.READ_ONLY, boolToInt(linkCards.isReadOnly())));
		parameters.add(parameterFor(LinkCardsWidgetFactory.REQUIRED, boolToInt(linkCards.isRequired())));
		parameters.add(parameterFor(LinkCardsWidgetFactory.FILTER, linkCards.getFilter()));
		parameters.add(parameterFor(LinkCardsWidgetFactory.DEFAULT_SELECTION, linkCards.getDefaultSelection()));
		parameters.add(parameterFor(AdditionalConstants.OUTPUT_NAME, linkCards.getOutputName()));
		definition.setParameters(parameters);
	}

	@Legacy("no comment")
	private int boolToInt(final boolean value) {
		return value ? 1 : 0;
	}

	@Override
	public void visit(final ManageEmail manageEmail) {
		// TODO when will be a need
	}

	@Override
	public void visit(final ManageRelation manageRelation) {
		// TODO when will be a need
	}

	@Override
	public void visit(final OpenAttachment openAttachment) {
		// nothing to do
	}

	@Override
	public void visit(final OpenNote openNote) {
		// nothing to do
	}

	@Override
	public void visit(final OpenReport openReport) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, openReport.getLabel()));
		parameters.add(parameterFor(LegacyConstants.REPORT_TYPE, LegacyConstants.DEFAULT_REPORT_TYPE_AS_STRING));
		parameters.add(parameterFor(OpenReportWidgetFactory.REPORT_CODE, openReport.getReportCode()));
		parameters.add(parameterFor(LegacyConstants.ID, reportIdFor(openReport)));
		parameters.add(parameterFor(OpenReportWidgetFactory.STORE_IN_PROCESS, false));
		parameters.add(parameterFor(LegacyConstants.FORCE_EXTENSION, openReport.getForceFormat()));
		for (final Entry<String, Object> entry : openReport.getPreset().entrySet()) {
			parameters.add(parameterFor(entry.getKey(), entry.getValue()));
		}
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final NavigationTree navigationTree) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, navigationTree.getLabel()));
		for (final Entry<String, Object> entry : navigationTree.getPreset().entrySet()) {
			parameters.add(parameterFor(entry.getKey(), entry.getValue()));
		}
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final Grid grid) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, grid.getLabel()));
		for (final Entry<String, Object> entry : grid.getPreset().entrySet()) {
			parameters.add(parameterFor(entry.getKey(), entry.getValue()));
		}
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final Workflow workflow) {
		final List<WorkflowWidgetDefinitionParameter> parameters = new ArrayList<WorkflowWidgetDefinitionParameter>();
		parameters.add(parameterFor(ValuePairWidgetFactory.BUTTON_LABEL, workflow.getLabel()));
		for (final Entry<String, Object> entry : workflow.getPreset().entrySet()) {
			parameters.add(parameterFor(entry.getKey(), entry.getValue()));
		}
		definition.setParameters(parameters);
	}

	@Override
	public void visit(final Ping ping) {
		// TODO when will be a need
	}

	@Override
	public void visit(final WebService webService) {
		// TODO when will be a need
	}

	@Override
	public void visit(final PresetFromCard presetFromCard) {
		// TODO when will be a need
	}

	private int reportIdFor(final OpenReport openReport) {
		final ReportStore reportStore = applicationContext().getBean(JDBCReportStore.class);
		final Report reportCard = reportStore.findReportByTypeAndCode(LegacyConstants.DEFAULT_REPORT_TYPE,
				openReport.getReportCode());

		if (reportCard == null) {
			throw ReportExceptionType.REPORT_NOTFOUND.createException(openReport.getReportCode());
		}

		return reportCard.getId();
	}

	private WorkflowWidgetDefinitionParameter parameterFor(final String key, final Object value) {
		final WorkflowWidgetDefinitionParameter parameter;
		if (value == null) {
			parameter = parameterFor(key, EMPTY);
		} else if (value instanceof Number) {
			parameter = parameterFor(key, Number.class.cast(value).intValue());
		} else if (value instanceof Boolean) {
			parameter = parameterFor(key, Boolean.class.cast(value));
		} else {
			parameter = parameterFor(key, value.toString());
		}
		return parameter;
	}

	private WorkflowWidgetDefinitionParameter parameterFor(final String key, final boolean value) {
		return parameterFor(key, Boolean.toString(value));
	}

	private WorkflowWidgetDefinitionParameter parameterFor(final String key, final int value) {
		return parameterFor(key, Integer.toString(value));
	}

	private WorkflowWidgetDefinitionParameter parameterFor(final String key, final String value) {
		final WorkflowWidgetDefinitionParameter parameter = new WorkflowWidgetDefinitionParameter();
		parameter.setKey(key);
		parameter.setValue(value);
		return parameter;
	}

}
