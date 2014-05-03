package org.cmdbuild.model.widget;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;

public class Workflow extends Widget {

	private String workflowName;
	private String filter;
	private String filterType;
	private String outputName;

	public static final String SUBMISSION_PARAM = "output";
	private static final String CARDID_FIELD = "id";
	private static final String CLASSNAME_FIELD = "className";

	private Map<String, Object> preset;

	public String getFilterType() {
		return filterType;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public static class Submission {
		private Object output;

		public Object getOutput() {
			return output;
		}

		public void setOutput(final Object output) {
			this.output = output;
		}
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			final Submission submission = decodeInput(input);
			output.put(outputName, outputValue(submission));
		}
	}

	private Submission decodeInput(final Object input) {
		if (input instanceof Submission) {
			return (Submission) input;
		} else {
			@SuppressWarnings("unchecked")
			final Map<String, Object> inputMap = (Map<String, Object>) input;
			Object output = inputMap.get(SUBMISSION_PARAM);
			final Submission submission = new Submission();
			submission.setOutput(output);
			return submission;
		}
	}

	private Reference outputValue(final Submission submission) {
		final Object output = submission.getOutput();
		@SuppressWarnings("unchecked")
		final Map<String, String> idAndClassname = (Map<String, String>) output;
		final Object id = idAndClassname.get(CARDID_FIELD);
		final String className = idAndClassname.get(CLASSNAME_FIELD);
		if(id != null){
			final Reference outputReference = new Reference() {
				@Override
				public Long getId() {
					return toLong(id);
				}
				@Override
				public String getClassName() {
					return className;
				}
			};
			return outputReference;
		}else{
			final Reference outputReference = new Reference() {
				@Override
				public Long getId() {
					return toLong(-1);
				}
				@Override
				public String getClassName() {
					return className;
				}
			};
			return outputReference;
		}
	}

	private Long toLong(final Object cardId) {
		return new AbstractReferenceAttributeType() {

			@Override
			public void accept(final CMAttributeTypeVisitor visitor) {
				throw new UnsupportedOperationException();
			}

		}.convertValue(cardId).getId();
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}
}
