package org.cmdbuild.config;

import org.cmdbuild.workflow.service.RemoteSharkServiceConfiguration;

public interface WorkflowConfiguration extends RemoteSharkServiceConfiguration {

	boolean isEnabled();

}
