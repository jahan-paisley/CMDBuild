package org.cmdbuild.workflow.service;

import org.enhydra.shark.api.client.wfmc.wapi.WMProcessDefinition;

public class WSProcessDefInfoImpl implements WSProcessDefInfo {

	protected String packageId;
	protected String packageVersion;
	protected String processDefinitionId;

	static WSProcessDefInfo newInstance(final WMProcessDefinition processDefinition) {
		final WSProcessDefInfoImpl instance = new WSProcessDefInfoImpl();
		instance.packageId = processDefinition.getPackageId();
		instance.packageVersion = processDefinition.getVersion();
		instance.processDefinitionId = processDefinition.getId();
		return instance;
	}

	public static WSProcessDefInfo newInstance(final String pkgId, final String pkgVer, final String procDefId) {
		final WSProcessDefInfoImpl instance = new WSProcessDefInfoImpl();
		instance.packageId = pkgId;
		instance.packageVersion = pkgVer;
		instance.processDefinitionId = procDefId;
		return instance;
	}

	@Override
	public final String getPackageId() {
		return packageId;
	}

	@Override
	public final String getPackageVersion() {
		return packageVersion;
	}

	@Override
	public final String getProcessDefinitionId() {
		return processDefinitionId;
	}

}
