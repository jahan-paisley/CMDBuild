package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;

public interface XpdlExtendedAttributeWidgetFactory {

	CMActivityWidget createWidget(XpdlExtendedAttribute xa, CMValueSet processInstanceVariables);
}
