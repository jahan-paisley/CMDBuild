package org.cmdbuild.model.widget;

import java.util.HashMap;
import java.util.Map;

public class PresetFromCard extends Widget {

	/**
	 * The name of the class for which
	 * display the cards
	 */
	private String className;

	/**
	 * An optional CQL filter to display
	 * a subset of cards.
	 * If used, the className is ignored
	 */
	private String filter;

	/**
	 * Templates to use for the CQL filter
	 */
	private Map<String, String> templates;

	/**
	 * The mapping to use to preset the
	 * activity, after the selection of
	 * a card
	 */
	private Map<String, String> presetMapping;

	public PresetFromCard() {
		className = "";
		filter = "";
		templates = new HashMap<String, String>();
		presetMapping = new HashMap<String, String>();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	public Map<String, String> getPresetMapping() {
		return presetMapping;
	}

	public void setPresetMapping(Map<String, String> presetMapping) {
		this.presetMapping = presetMapping;
	}

	@Override
	public void accept(WidgetVisitor visitor) {
		visitor.visit(this);
	}
}