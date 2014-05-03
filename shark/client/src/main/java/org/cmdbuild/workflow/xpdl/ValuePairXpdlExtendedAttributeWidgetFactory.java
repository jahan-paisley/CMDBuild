package org.cmdbuild.workflow.xpdl;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;

/**
 * Widget factory that creates widgets from a key/value representation as the
 * value of an extended attribute. The type is defined in the extended attribute
 * key.
 */
@ThreadSafe
public class ValuePairXpdlExtendedAttributeWidgetFactory implements XpdlExtendedAttributeWidgetFactory {

	final Map<String, ActivityWidgetFactory> factories;

	public ValuePairXpdlExtendedAttributeWidgetFactory() {
		factories = new HashMap<String, ActivityWidgetFactory>();
	}

	public void addWidgetFactory(final SingleActivityWidgetFactory wf) {
		Validate.notNull(wf);
		factories.put(wf.getWidgetName(), wf);
	}

	@Override
	public CMActivityWidget createWidget(final XpdlExtendedAttribute xa, final CMValueSet processInstanceVariables) {
		final String name = xa.getKey();
		final String serialization = xa.getValue();
		final ActivityWidgetFactory f = factories.get(name);
		if (f != null && serialization != null) {
			return f.createWidget(serialization, processInstanceVariables);
		}
		return null;
	}

}
