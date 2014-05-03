package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;

/**
 * Creates an activity widget given its serialization.
 */
public interface ActivityWidgetFactory {

	CMActivityWidget createWidget(String serialization, CMValueSet processVariables);
}
