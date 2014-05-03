package org.cmdbuild.model.widget;

import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.model.widget.WidgetVisitor.WidgetVisitable;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMActivityWidget;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@NotThreadSafe
public abstract class Widget implements CMActivityWidget, WidgetVisitable, Storable {

	protected interface WidgetAction {
		Object execute() throws Exception;
	}

	@JsonProperty("id")
	private String identifier;
	private String label;
	private boolean active;
	private boolean alwaysenabled;
	@JsonIgnore
	private String sourceClass;

	protected Widget() {
		label = StringUtils.EMPTY;
		setActive(true);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@JsonIgnore
	public void setId(final Long id) {
		setStringId((id == null) ? null : Long.toString(id));
	}

	@Override
	public final Object executeAction(final String action, final Map<String, Object> params,
			final Map<String, Object> dsVars) throws Exception {
		final WidgetAction actionCommand = getActionCommand(action, params, dsVars);
		if (actionCommand != null) {
			return actionCommand.execute();
		}
		final String error = String.format("Action not defined for widget %s", getClass().getCanonicalName());
		throw new UnsupportedOperationException(error);
	}

	/**
	 * Returns the WidgetAction object for the action by that name. If no action
	 * matches, then it should return null.
	 * 
	 * @param action
	 *            (can be null)
	 * @return a widget action or null
	 */
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> params,
			final Map<String, Object> dsVars) {
		return null;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
	}

	@Override
	public void advance(final CMActivityInstance activityInstance) {
	}

	public final void setStringId(final String id) {
		this.identifier = id;
	}

	@JsonIgnore
	@Override
	public final String getStringId() {
		return identifier;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public final String getLabel() {
		return label;
	}

	public final void setActive(final boolean active) {
		this.active = active;
	}

	public final boolean isActive() {
		return active;
	}

	public String getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(final String sourceClass) {
		this.sourceClass = sourceClass;
	}

	public final void setAlwaysenabled(final boolean alwaysenabled) {
		this.alwaysenabled = alwaysenabled;
	}

	@Override
	public final boolean isAlwaysenabled() {
		return alwaysenabled;
	}

	@Override
	public final boolean equals(final Object obj) {
		if (obj != null && obj instanceof Widget) {
			final Widget other = (Widget) obj;
			return this.getStringId().equals(other.getStringId());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return getStringId().hashCode();
	}

	/*
	 * HACK to serialize type information in lists
	 */

	public final void setType(final String type) {
	}

	public final String getType() {
		final String fullName = this.getClass().getName();
		return fullName.substring(fullName.lastIndexOf("."));
	}

}
