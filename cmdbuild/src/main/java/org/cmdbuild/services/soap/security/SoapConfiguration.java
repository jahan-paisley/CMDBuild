package org.cmdbuild.services.soap.security;

import java.util.Set;

import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

public class SoapConfiguration implements Configuration {

	private final Configuration configuration;

	@Autowired
	public SoapConfiguration( //
			final Configuration configuration //
	) {
		this.configuration = configuration;
	}

	@Override
	public Set<String> getActiveAuthenticators() {
		return Sets.newHashSet(SoapPasswordAuthenticator.class.getSimpleName());
	}

	@Override
	public Set<String> getServiceUsers() {
		return configuration.getServiceUsers();
	}

	@Override
	public Set<String> getPrivilegedServiceUsers() {
		return configuration.getPrivilegedServiceUsers();
	}

}
