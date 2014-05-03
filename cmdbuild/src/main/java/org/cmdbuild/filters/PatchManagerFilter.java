package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.startup.StartupLogic;
import org.cmdbuild.spring.annotations.FilterComponent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@FilterComponent("PatchManagerFilter")
public class PatchManagerFilter implements Filter, ApplicationContextAware {

	private static final String JSP_PAGE = "patchmanager.jsp";

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private StartupLogic startupLogic() {
		final StartupLogic startupLogic = applicationContext.getBean(StartupLogic.class);
		return startupLogic;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = ((HttpServletRequest) request);
		if (isApplicable(httpRequest) && startupLogic().migrationRequired()) {
			request.getRequestDispatcher(JSP_PAGE).forward(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
	}

	protected boolean isApplicable(final HttpServletRequest request) {
		final String document = request.getRequestURI();
		final boolean isException = document.indexOf("configure") > -1 || document.indexOf("util") > -1
				|| document.matches("^(.*)(css|js|png|jpg|gif)$");
		return !isException;
	}
}
