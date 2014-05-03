package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;

public class XpdlActivityWrapper implements CMActivity {

	private static CMValueSet UNAVAILABLE_PROCESS_INSTANCE = new CMValueSet() {

		final String EXCEPTION = "Process instance not available";

		@Override
		public Object get(final String key) {
			throw new UnsupportedOperationException(EXCEPTION);
		}

		@Override
		public <T> T get(final String key, final Class<? extends T> requiredType) {
			throw new UnsupportedOperationException(EXCEPTION);
		}

		@Override
		public Iterable<Entry<String, Object>> getValues() {
			throw new UnsupportedOperationException(EXCEPTION);
		}

	};

	@Legacy("As in 1.x")
	public static final String ADMIN_START_XA = "AdminStart";

	private final XpdlActivity inner;
	private final XpdlExtendedAttributeVariableFactory variableFactory;
	private final XpdlExtendedAttributeWidgetFactory widgetFactory;

	public XpdlActivityWrapper(final XpdlActivity xpdlActivity,
			final XpdlExtendedAttributeVariableFactory variableFactory,
			final XpdlExtendedAttributeWidgetFactory widgetFactory) {
		Validate.notNull(xpdlActivity, "Wrapped object cannot be null");
		Validate.notNull(variableFactory, "Wrapped object cannot be null");
		Validate.notNull(widgetFactory, "Wrapped object cannot be null");
		this.inner = xpdlActivity;
		this.variableFactory = variableFactory;
		this.widgetFactory = widgetFactory;
	}

	@Override
	public List<ActivityPerformer> getPerformers() {
		final List<ActivityPerformer> out = new ArrayList<ActivityPerformer>();
		out.add(getFirstNonAdminPerformer());
		if (isAdminStart()) {
			out.add(ActivityPerformer.newAdminPerformer());
		}
		return out;
	}

	@Legacy("As in 1.x")
	private boolean isAdminStart() {
		return inner.hasExtendedAttributeIgnoreCase(ADMIN_START_XA);
	}

	@Override
	public String getId() {
		return inner.getId();
	}

	@Override
	public String getDescription() {
		return inner.getName();
	}

	@Override
	public String getInstructions() {
		return inner.getDescription();
	}

	@Override
	public ActivityPerformer getFirstNonAdminPerformer() {
		final String performerString = inner.getFirstPerformer();
		if (performerString == null) {
			return ActivityPerformer.newUnknownPerformer();
		}
		if (inner.getProcess().hasRoleParticipant(performerString)) {
			return ActivityPerformer.newRolePerformer(performerString);
		} else {
			return ActivityPerformer.newExpressionPerformer(performerString);
		}
	}

	@Override
	public List<CMActivityVariableToProcess> getVariables() {
		final List<CMActivityVariableToProcess> vars = new ArrayList<CMActivityVariableToProcess>();
		for (final XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityVariableToProcess v = variableFactory.createVariable(xa);
			if (v != null) {
				vars.add(v);
			}
		}
		return vars;
	}

	@Override
	public List<CMActivityWidget> getWidgets() {
		return getWidgets(UNAVAILABLE_PROCESS_INSTANCE);
	}

	@Override
	public List<CMActivityWidget> getWidgets(final CMValueSet processInstanceVariables) {
		final List<CMActivityWidget> widgets = new ArrayList<CMActivityWidget>();
		for (final XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityWidget w = widgetFactory.createWidget(xa, processInstanceVariables);
			if (w != null) {
				widgets.add(w);
			}
		}
		return widgets;
	}
}
