package org.cmdbuild.model.widget;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;

public class LinkCards extends Widget {

	public static class Submission {
		private List<Object> output;

		public List<Object> getOutput() {
			return output;
		}

		public void setOutput(final List<Object> output) {
			this.output = output;
		}
	}

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	/**
	 * A CQL query to fill the linkCard grid Use it or the className
	 */
	private String filter;

	/**
	 * Fill the linkCard grid with the cards of this class. Use it or the filter
	 */
	private String className;

	/**
	 * A CQL query to define the starting selection
	 */
	private String defaultSelection;

	/**
	 * If true, the grid is in read-only mode so you can not select its rows
	 */
	private boolean readOnly;

	/**
	 * To allow the selection of only a row
	 */
	private boolean singleSelect;

	/**
	 * Add an icon at the right of each row to edit the referred card
	 */
	private boolean allowCardEditing;

	/**
	 * If true, the user must select a card on this widget before to can advance
	 * with the process
	 */
	private boolean required;

	/**
	 * If true, enable the map module for this widget
	 */
	private boolean enableMap;

	/**
	 * The latitude to use as default for the map module
	 */
	private Integer mapLatitude;

	/**
	 * The longitude to use as default for the map module
	 */
	private Integer mapLongitude;

	/**
	 * The zoom level to use as default for the map module
	 */
	private Integer mapZoom;

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	/**
	 * Templates to use for the CQL filters
	 */
	private Map<String, String> templates;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getDefaultSelection() {
		return defaultSelection;
	}

	public void setDefaultSelection(final String defaultSelection) {
		this.defaultSelection = defaultSelection;
	}

	public boolean isSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(final boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	public boolean isAllowCardEditing() {
		return allowCardEditing;
	}

	public void setAllowCardEditing(final boolean allowCardEditing) {
		this.allowCardEditing = allowCardEditing;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isEnableMap() {
		return enableMap;
	}

	public void setEnableMap(final boolean enableMap) {
		this.enableMap = enableMap;
	}

	public Integer getMapLatitude() {
		return mapLatitude;
	}

	public void setMapLatitude(final Integer mapLatitude) {
		this.mapLatitude = mapLatitude;
	}

	public Integer getMapLongitude() {
		return mapLongitude;
	}

	public void setMapLongitude(final Integer mapLongitude) {
		this.mapLongitude = mapLongitude;
	}

	public Integer getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(final Integer mapZoom) {
		this.mapZoom = mapZoom;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(final Map<String, String> templates) {
		this.templates = templates;
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
		final List<Object> selectedCardIds = submission.getOutput();
		final List<Reference> selectedCards = newArrayListWithExpectedSize(selectedCardIds.size());
		for (final Object cardId : selectedCardIds) {
			final Long cardIdLong = toLong(cardId);
			final Reference reference = new Reference() {

				@Override
				public Long getId() {
					return cardIdLong;
				}

				@Override
				public String getClassName() {
					return null;
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

}
