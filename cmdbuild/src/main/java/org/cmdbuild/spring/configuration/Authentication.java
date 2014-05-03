package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SOAP;
import java.util.Arrays;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.LdapAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogicBuilder;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.soap.security.SoapConfiguration;
import org.cmdbuild.services.soap.security.SoapPasswordAuthenticator;
import org.cmdbuild.services.soap.security.SoapUserFetcher;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Authentication {

	@Autowired
	private AuthenticationStore authenticationStore;

	@Autowired
	private AuthProperties authProperties;

	@Autowired
	private Filter filter;

	@Autowired
	private SoapConfiguration soapConfiguration;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserStore userStore;

	@Autowired
	private ViewConverter viewConverter;

	@Autowired
	private PrivilegeContextFactory privilegeContextFactory;

	@Bean
	@Qualifier(DEFAULT)
	protected LegacyDBAuthenticator dbAuthenticator() {
		return new LegacyDBAuthenticator(systemDataView);
	}

	@Bean
	@Qualifier(SOAP)
	protected SoapUserFetcher soapUserFetcher() {
		return new SoapUserFetcher(systemDataView, authenticationStore);
	}

	@Bean
	protected SoapPasswordAuthenticator soapPasswordAuthenticator() {
		return new SoapPasswordAuthenticator();
	}

	@Bean
	protected CasAuthenticator casAuthenticator() {
		return new CasAuthenticator(authProperties);
	}

	@Bean
	protected HeaderAuthenticator headerAuthenticator() {
		return new HeaderAuthenticator(authProperties);
	}

	@Bean
	protected LdapAuthenticator ldapAuthenticator() {
		return new LdapAuthenticator(authProperties);
	}

	@Bean
	@Scope(PROTOTYPE)
	public DBGroupFetcher dbGroupFetcher() {
		return new DBGroupFetcher(systemDataView, Arrays.asList( //
				new CMClassPrivilegeFetcherFactory(systemDataView), //
				new ViewPrivilegeFetcherFactory(systemDataView, viewConverter), //
				new FilterPrivilegeFetcherFactory(systemDataView, filter.dataViewFilterStore())));
	}

	@Bean
	@Qualifier(DEFAULT)
	public AuthenticationService defaultAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(authProperties,
				systemDataView);
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	@Qualifier(SOAP)
	public AuthenticationService soapAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(soapConfiguration,
				systemDataView);
		authenticationService.setPasswordAuthenticators(soapPasswordAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), soapUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		authenticationService.setUserStore(userStore);
		return authenticationService;
	}

	@Bean
	@Scope(PROTOTYPE)
	public DefaultAuthenticationLogicBuilder defaultAuthenticationLogicBuilder() {
		return new DefaultAuthenticationLogicBuilder( //
				defaultAuthenticationService(), //
				privilegeContextFactory, //
				systemDataView, //
				userStore);
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(SOAP)
	public SoapAuthenticationLogicBuilder soapAuthenticationLogicBuilder() {
		return new SoapAuthenticationLogicBuilder( //
				soapAuthenticationService(), //
				privilegeContextFactory, //
				systemDataView, //
				userStore);
	}

}
