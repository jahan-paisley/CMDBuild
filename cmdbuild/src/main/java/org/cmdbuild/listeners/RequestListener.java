package org.cmdbuild.listeners;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.spring.annotations.CmdbuildComponent;

import com.google.common.collect.Lists;

@CmdbuildComponent
public class RequestListener implements ServletRequestListener, Notifier {

	public class CMDBContext {

		private final HttpServletRequest request;
		private final List<CMDBException> warnings = Lists.newLinkedList();

		private CMDBContext(final HttpServletRequest request) {
			this.request = request;
		}

		public void pushWarning(final CMDBException e) {
			warnings.add(e);
		}

		public List<CMDBException> getWarnings() {
			return warnings;
		}

		private HttpServletRequest getRequest() {
			return request;
		}

	}

	private static ThreadLocal<CMDBContext> requestContext = new ThreadLocal<CMDBContext>();

	public static CMDBContext getCurrentRequest() {
		return requestContext.get();
	}

	public Object getCurrentSessionObject(final String name) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			return session.getAttribute(name);
		} else {
			return null;
		}
	}

	public void setCurrentSessionObject(final String name, final Object value) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			session.setAttribute(name, value);
		}
	}

	public void removeCurrentSessionObject(final String name) {
		final HttpSession session = getOrCreateSession();
		if (session != null) {
			session.removeAttribute(name);
		}
	}

	private HttpSession getOrCreateSession() {
		final CMDBContext ctx = getCurrentRequest();
		HttpSession session = null;
		if (ctx != null) {
			ctx.getRequest().getSession(false);
			if (session == null) {
				session = ctx.getRequest().getSession(true);
				initSession(session);
			}
		}
		return session;
	}

	private void initSession(final HttpSession session) {
		final int sessionTimeout = applicationContext() //
				.getBean(CmdbuildConfiguration.class) //
				.getSessionTimoutOrZero();
		if (sessionTimeout > 0) {
			session.setMaxInactiveInterval(sessionTimeout);
		}
	}

	@Override
	public void requestInitialized(final ServletRequestEvent sre) {
		final ServletRequest req = sre.getServletRequest();
		if (req instanceof HttpServletRequest) {
			final CMDBContext currentRequestContext = new CMDBContext((HttpServletRequest) req);
			requestContext.set(currentRequestContext);
		}
	}

	@Override
	public void requestDestroyed(final ServletRequestEvent sre) {
		requestContext.remove();
	}

	@Override
	public void warn(final CMDBException e) {
		getCurrentRequest().pushWarning(e);
	}

}
