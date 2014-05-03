package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class CmdbfProperties extends DefaultProperties implements CmdbfConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "cmdbf";

	private static final String MDR_ID = "mdrid";
	private static final String SCHEMA_LOCATION = "schemalocation";

	public CmdbfProperties() {
		super();
		setProperty(MDR_ID, "http://www.cmdbuild.org");
		setProperty(SCHEMA_LOCATION, "http://localhost:8080/cmdbuild/services/cmdb-schema");
	}

	public static CmdbfProperties getInstance() {
		return (CmdbfProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public String getMdrId() {
		return getProperty(MDR_ID);
	}

	@Override
	public void setMdrId(final String mdrId) {
		setProperty(MDR_ID, mdrId);
	}

	@Override
	public String getSchemaLocation() {
		return getProperty(SCHEMA_LOCATION);
	}

	@Override
	public void setSchemaLocation(final String schemaLocation) {
		setProperty(SCHEMA_LOCATION, schemaLocation);
	}

}
