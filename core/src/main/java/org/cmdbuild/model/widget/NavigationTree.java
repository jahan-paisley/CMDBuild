package org.cmdbuild.model.widget;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;

public class NavigationTree extends Widget {

	private String name, description;
	private String filter;
	private Map<String, Object> preset;
	private String outputName;

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";
	private static final String CLASSNAME_FIELD = "className";
	private static final String CARDID_FIELD = "cardId";

	public static class Submission {
		private List<Object> output;

		public List<Object> getOutput() {
			return output;
		}

		public void setOutput(final List<Object> output) {
			this.output = output;
		}
	}

	public String getNavigationTreeName() {
		return name;
	}

	public void setNavigationTreeName(final String navigationTreeName) {
		this.name = navigationTreeName;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
			final Map<String, List<Object>> inputMap = (Map<String, List<Object>>) input;
			final List<Object> selectedCardIds = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
			final Submission submission = new Submission();
			submission.setOutput(selectedCardIds);
			return submission;
		}
	}

	private Reference[] outputValue(final Submission submission) {
		final List<Object> selectedCardIdAndClassname = submission.getOutput();
		final List<Reference> selectedCards = newArrayListWithExpectedSize(selectedCardIdAndClassname.size());
		for (final Object idAndClassname : selectedCardIdAndClassname) {
			@SuppressWarnings("unchecked")
			final Map<String, String> entry = (Map<String, String>) idAndClassname;
			final Long cardIdLong = toLong(entry.get(CARDID_FIELD));
			final String className = entry.get(CLASSNAME_FIELD);
			final Reference reference = new Reference() {

				@Override
				public Long getId() {
					return cardIdLong;
				}

				@Override
				public String getClassName() {
					return className;
				}

			};
			selectedCards.add(reference);
		}
		return selectedCards.toArray(new Reference[selectedCards.size()]);
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
