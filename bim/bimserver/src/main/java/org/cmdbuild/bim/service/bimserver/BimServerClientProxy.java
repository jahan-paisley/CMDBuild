package org.cmdbuild.bim.service.bimserver;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.soap.SoapBimServerClientFactory;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;

public class BimServerClientProxy {

	BimServerClient client;
	boolean connectionStatus;

	boolean connect(BimserverConfiguration configuration) {
		if (configuration.isEnabled()) {
			if (isConnected()) {
				connectionStatus = true;
			} else {
				BimServerClientFactory factory = new SoapBimServerClientFactory(configuration.getUrl());
				try {
					client = factory.create(new UsernamePasswordAuthenticationInfo(configuration.getUsername(),
							configuration.getPassword()));
					connectionStatus = true;
				} catch (final Throwable t) {
					connectionStatus = false;
					System.out.println("Connection to BimServer failed");
				}
			}
		} else {
			connectionStatus = false;
		}
		return connectionStatus;
	}

	public boolean isConnected() {
		boolean pingSuccess = false;
		if (connectionStatus) {
			try {
				pingSuccess = client.getBimsie1AuthInterface().isLoggedIn();
			} catch (Throwable t) {
			}
		}
		return pingSuccess;
	}

}
