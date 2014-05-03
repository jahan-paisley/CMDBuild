package org.cmdbuild.services.soap.utils;

import java.util.List;

import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.message.Message;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class WebserviceUtils {

	public String getAuthData(final MessageContext messageContext) {
		String authData = null;
		if (messageContext != null) {
			final List<?> v = (List<?>) messageContext.get(WSHandlerConstants.RECV_RESULTS);
			authData = authData(v);
		}
		return authData;
	}

	public String getAuthData(final Message message) {
		final List<?> contextualPropertyAsList = (List<?>) message
				.getContextualProperty(WSHandlerConstants.RECV_RESULTS);
		return authData(contextualPropertyAsList);
	}

	private String authData(final List<?> values) {
		final WSHandlerResult results = (WSHandlerResult) values.get(0);
		final List<?> wsResults = results.getResults();
		final WSSecurityEngineResult ws = (WSSecurityEngineResult) wsResults.get(0);
		final Object rawPrincipal = ws.get(WSSecurityEngineResult.TAG_PRINCIPAL);
		final WSUsernameTokenPrincipal principal = WSUsernameTokenPrincipal.class.cast(rawPrincipal);
		return principal.getName();
	}

}
