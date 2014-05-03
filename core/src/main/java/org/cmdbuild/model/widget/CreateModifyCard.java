package org.cmdbuild.model.widget;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;

public class CreateModifyCard extends Widget {

	public static class Submission {
		private Object output;

		public Object getOutput() {
			return output;
		}

		public void setOutput(final Object output) {
			this.output = output;
		}
	}

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	private String idcardcqlselector;
	private String targetClass;
	private boolean readonly;
	private Map<String, Object> attributeMappingForCreation;

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getIdcardcqlselector() {
		return idcardcqlselector;
	}

	public void setIdcardcqlselector(final String idcardcqlselector) {
		this.idcardcqlselector = idcardcqlselector;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(final boolean readonly) {
		this.readonly = readonly;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public void setAttributeMappingForCreation(final Map<String, Object> attributeMappingForCreation) {
		this.attributeMappingForCreation = attributeMappingForCreation;
	}

	public Map<String, Object> getAttributeMappingForCreation() {
		return attributeMappingForCreation;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (readonly) {
			return;
		}
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
			final Object createdCardId = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
			final Submission submission = new Submission();
			submission.setOutput(createdCardId);
			return submission;
		}
	}

	private Reference outputValue(final Submission submission) {
		final Long createdCardId = toLong(submission.getOutput());
		return new Reference() {

			@Override
			public Long getId() {
				return createdCardId;
			};

			@Override
			public String getClassName() {
				return null;
			}

		};
	}
	
	private Long toLong(final Object cardId) {
		return new AbstractReferenceAttributeType() {

			@Override
			public void accept(final CMAttributeTypeVisitor visitor) {
				throw new UnsupportedOperationException();
			}

		}.convertValue(cardId).getId();
	}

}
