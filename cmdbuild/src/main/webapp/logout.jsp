<%@ page language="java" %>
<%@ page session="true" %>
<%@ page import="org.cmdbuild.filters.AuthFilter"%>
<%
	if (session != null) {
		session.invalidate();
	}
	response.sendRedirect(AuthFilter.LOGIN_URL);
%>
