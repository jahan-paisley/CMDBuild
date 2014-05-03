package org.cmdbuild.model.widget;

import java.util.Map;

public class Grid extends Widget {

	private String className;
	private String filter;
	private Map<String, Object> preset;

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

	@Override
	public void accept(final WidgetVisitor visitor) {
//		visitor.visit(this);
	}

	public void setPreset(final Map<String, Object> preset) {
		this.preset = preset;
	}

	public Map<String, Object> getPreset() {
		return preset;
	}
}