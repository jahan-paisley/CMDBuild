package org.cmdbuild.workflow.service;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;

/**
 * Implementation using remote Shark server
 */
public class LocalSharkService extends TransactedSharkService {

	public interface Config {
		String getUsername();
	}

	private final Config config;

	public LocalSharkService(final Config config) {
		super(getClientProperties());
		this.config = config;
	}

	private static Properties getClientProperties() {
		final Properties clientProps = new Properties();
		clientProps.put("SharkConfResourceLocation", "Shark.conf");
		return clientProps;
	}

	@Override
	protected WMConnectInfo getConnectionInfo() {
		return new WMConnectInfo(config.getUsername(), StringUtils.EMPTY, DEFAULT_ENGINE_NAME, DEFAULT_SCOPE);
	}

}
