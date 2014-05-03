package org.cmdbuild.auth;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LdapAuthenticator implements PasswordAuthenticator {

	public static interface Configuration {

		String getLdapUrl();

		String getLdapBaseDN();

		String getLdapSearchFilter();

		String getLdapBindAttribute();

		String getLdapAuthenticationMethod();

		String getLdapPrincipal();

		String getLdapPrincipalCredentials();

	}

	private static final Marker marker = MarkerFactory.getMarker(LdapAuthenticator.class.getName());

	private static final String INITIAL_CONTEXT_FACTORY = com.sun.jndi.ldap.LdapCtxFactory.class.getName();

	private final Configuration configuration;

	public LdapAuthenticator(final Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getName() {
		return "LdapAuthenticator";
	}

	@Override
	public boolean checkPassword(final Login login, final String password) {
		try {
			final DirContext ctx = initialize();
			final String ldapUser = getUser(ctx, login.getValue());
			if ((ldapUser != null) && bind(ctx, ldapUser, password)) {
				return true;
			}
		} catch (final NamingException e) {
			logger.warn(marker, "authentication error", e);
		}
		logger.warn(marker, "cannot authenticate user '{}' on LDAP", login.getValue());
		return false;
	}

	private DirContext initialize() throws NamingException {
		final Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, configuration.getLdapUrl());
		env.put(Context.REFERRAL, "follow");
		try {
			final DirContext ctx = new InitialDirContext(env);
			setOriginalAuthentication(ctx);
			return ctx;
		} catch (final NamingException e) {
			logger.warn(marker, "cannot set LDAP properties", e);
			throw e;
		}
	}

	private String getUser(final DirContext ctx, final String userToFind) {
		final SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String usertobind = null;
		final String searchFilter = generateSearchFilter(userToFind);
		try {
			final NamingEnumeration<SearchResult> results = ctx.search(configuration.getLdapBaseDN(), searchFilter, sc);
			if (results.hasMore()) {
				final SearchResult sr = results.next();
				usertobind = sr.getNameInNamespace();
			}
		} catch (final NamingException e) {
			logger.warn("LDAP error", e);
			assert usertobind == null;
		}
		return usertobind;
	}

	private String generateSearchFilter(final String userToFind) {
		final String searchFilter = configuration.getLdapSearchFilter();
		String searchQuery;
		if (searchFilter != null) {
			searchQuery = String.format("(&%s(%s=%s))", configuration.getLdapSearchFilter(),
					configuration.getLdapBindAttribute(), userToFind);
		} else {
			searchQuery = String.format("(%s=%s)", configuration.getLdapBindAttribute(), userToFind);
		}
		logger.debug(marker, "LDAP generated search query: " + searchQuery.toString());
		return searchQuery.toString();
	}

	private boolean bind(final DirContext ctx, final String username, final String password) throws NamingException {
		boolean validate = false;
		try {
			logger.debug(marker, "setting simple bind to authenticate");
			ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
			logger.debug(marker, "binding with username '{}'", username);
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, username);
			logger.trace(marker, "binding with password '{}'", password);
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			ctx.getAttributes(EMPTY, null);
			validate = true;
		} catch (final NamingException e) {
			logger.warn(marker, "error while binding", e);
			logger.info(marker, "cannot execute LDAP authentication for user '{}'", username);
			setOriginalAuthentication(ctx);
			validate = false;
		} finally {
			// Terminate context
			ctx.close();
		}
		return validate;
	}

	private void setOriginalAuthentication(final DirContext ctx) throws NamingException {
		logger.debug(marker, "restoring defaults");
		ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, configuration.getLdapAuthenticationMethod());
		if (isNotEmpty(configuration.getLdapPrincipal())) {
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, configuration.getLdapPrincipal());
		}
		if (isNotEmpty(configuration.getLdapPrincipalCredentials())) {
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, configuration.getLdapPrincipalCredentials());
		}
	}

	@Override
	public String fetchUnencryptedPassword(final Login login) {
		return null;
	}

	@Override
	public PasswordChanger getPasswordChanger(final Login login) {
		return null;
	}

}
