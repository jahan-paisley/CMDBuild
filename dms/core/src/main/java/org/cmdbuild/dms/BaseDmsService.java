package org.cmdbuild.dms;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DmsConfiguration.NullDmsConfiguration;

public abstract class BaseDmsService implements DmsService {

	private static final DmsConfiguration NULL_CONFIGURATION = NullDmsConfiguration.newInstance();

	private DmsConfiguration configuration;

	@Override
	public DmsConfiguration getConfiguration() {
		return (configuration == null) ? NULL_CONFIGURATION : configuration;
	}

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		this.configuration = configuration;
	}

}
