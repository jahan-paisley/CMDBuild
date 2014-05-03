package org.cmdbuild.services.soap.operation;

import static java.util.Arrays.asList;

import java.util.List;

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
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmissionParameter;

class WidgetSubmissionConverter implements WidgetVisitor {

	private static final int ONE_PARAMETER_ONLY_EXPECTED = 0;
	private static final int SINGLE_VALUE_EXPECTED = 0;

	private final Widget widget;

	private List<WorkflowWidgetSubmissionParameter> parameters;
	private Object submissionOutput;

	public WidgetSubmissionConverter(final Widget widget) {
		this.widget = widget;
	}

	public Object convertFrom(final WorkflowWidgetSubmission submission) {
		parameters = asList(submission.getParameters());
		widget.accept(this);
		return submissionOutput;
	}

	@Override
	public void visit(final Calendar calendar) {
		// nothing to do
	}

	@Override
	public void visit(final CreateModifyCard createModifyCard) {
		final CreateModifyCard.Submission submission = new CreateModifyCard.Submission();
		if (!parameters.isEmpty()) {
			final List<String> values = asList(parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues());
			submission.setOutput(values.get(SINGLE_VALUE_EXPECTED));
			submissionOutput = submission;
		}
	}

	@Override
	public void visit(final LinkCards linkCards) {
		final LinkCards.Submission submission = new LinkCards.Submission();
		if (!parameters.isEmpty()) {
			parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues();
			final List<Object> values = asList(toObject(parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues()));
			submission.setOutput(values);
			submissionOutput = submission;
		}
	}

	private Object toObject(final String[] values) {
		return asList(values).toArray();
	}

	@Override
	public void visit(final ManageEmail manageEmail) {
		// not yet implemented
	}

	@Override
	public void visit(final ManageRelation manageRelation) {
		// nothing to do
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
		// nothing to do
	}

	@Override
	public void visit(final NavigationTree navigationTree) {
		// nothing to do
	}

	@Override
	public void visit(final Grid grid) {
		// nothing to do
	}

	@Override
	public void visit(final Workflow workflow) {
		// nothing to do
	}

	@Override
	public void visit(final Ping ping) {
		// nothing to do
	}

	@Override
	public void visit(final WebService WebService) {
		// nothing to do
	}

	@Override
	public void visit(final PresetFromCard presetFromCard) {
		// nothing to do
	}
}
