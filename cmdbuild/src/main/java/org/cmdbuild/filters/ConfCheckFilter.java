package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.config.DatabaseProperties;

public class ConfCheckFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = ((HttpServletRequest)request);

        // check if the application is configured
		if( isApplicable(httpRequest) &&
			!DatabaseProperties.getInstance().isConfigured())
		{
			request.getRequestDispatcher("configure.jsp").forward(request, response);
		}
		else {
            filterChain.doFilter(request, response);
		}

	}

	public void init(FilterConfig arg0) throws ServletException {
	}

    protected boolean isApplicable(HttpServletRequest request){
		String document = request.getRequestURI();
		boolean isException =
			document.indexOf("configure") > -1 ||
			document.indexOf("util") > -1 ||
			document.matches("^(.*)(css|js|png|jpg|gif)$");
		return ! isException;
    }

}
