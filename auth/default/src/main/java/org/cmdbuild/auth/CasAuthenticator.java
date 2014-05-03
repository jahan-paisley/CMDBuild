package org.cmdbuild.auth;

import org.apache.commons.lang3.Validate;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * CAS Single Sign-On authenticator
 */
public class CasAuthenticator implements ClientRequestAuthenticator {

	private static final Marker MARKER = MarkerFactory.getMarker(CasAuthenticator.class.getName());

	public static final String SKIP_SSO_PARAM = "skipsso";

	public interface Configuration {

		String getCasServerUrl();

		String getCasLoginPage();

		String getCasTicketParam();

		String getCasServiceParam();
	}

	public interface CasService {

		String getRedirectUrl(ClientRequest request);

		String getUsernameFromTicket(ClientRequest request);
	}

	/**
	 * Wraps ClientRequest to add the SKIP_SSO parameter to the URL
	 */
	private static class SkipSsoClientRequest implements ClientRequest {

		private final ClientRequest request;

		private SkipSsoClientRequest(final ClientRequest request) {
			this.request = request;
		}

		/*
		 * The request URL does never contain parameters
		 */
		@Override
		public String getRequestUrl() {
			return String.format("%s?%s", request.getRequestUrl(), SKIP_SSO_PARAM);
		}

		@Override
		public String getHeader(final String name) {
			return request.getHeader(name);
		}

		@Override
		public String getParameter(final String name) {
			return request.getParameter(name);
		}
	}

	private final CasService casService;

	public CasAuthenticator(final Configuration conf) {
		this(new CasServiceImpl(conf));
	}

	public CasAuthenticator(final CasService casService) {
		Validate.notNull(casService);
		this.casService = casService;
	}

	@Override
	public String getName() {
		return "CasAuthenticator";
	}

	@Override
	public Response authenticate(final ClientRequest request) {
		final ClientRequest skipSsoRequest = new SkipSsoClientRequest(request);

		final String userFromTicket = casService.getUsernameFromTicket(skipSsoRequest);
		if (userFromTicket != null) {
			final Login login = Login.newInstance(userFromTicket);
			logger.trace(MARKER, "authenticated as '{}'", userFromTicket);
			return Response.newLoginResponse(login);
		}

		if (skipAuthentication(request)) {
			return null;
		} else {
			final String redirectUrl = casService.getRedirectUrl(skipSsoRequest);
			logger.trace(MARKER, "redirecting to '{}'", redirectUrl);
			return Response.newRedirectResponse(redirectUrl);
		}
	}

	private boolean skipAuthentication(final ClientRequest request) {
		return request.getParameter(SKIP_SSO_PARAM) != null;
	}

	public static class CasServiceImpl implements CasService {

		private static final boolean CAS_RENEW = false;
		private static final boolean CAS_GATEWAY = false;
		private final Configuration conf;

		public CasServiceImpl(final Configuration conf) {
			Validate.notNull(conf);
			this.conf = conf;
		}

		@Override
		public String getRedirectUrl(final ClientRequest request) {
			return CommonUtils.constructRedirectUrl(conf.getCasServerUrl() + conf.getCasLoginPage(),
					conf.getCasServiceParam(), request.getRequestUrl(), CAS_RENEW, CAS_GATEWAY);
		}

		@Override
		public String getUsernameFromTicket(final ClientRequest request) {
			final String ticket = request.getParameter(conf.getCasTicketParam());
			if (ticket != null) {
				return validateTicket(ticket, request.getRequestUrl());
			} else {
				return null;
			}
		}

		private String validateTicket(final String ticket, final String service) {
			try {
				final TicketValidator ticketValidator = new Cas20ServiceTicketValidator(conf.getCasServerUrl());
				final Assertion assertion = ticketValidator.validate(ticket, service);
				return assertion.getPrincipal().getName();
			} catch (final TicketValidationException ex) {
				return null;
			}
		}
	}
}
