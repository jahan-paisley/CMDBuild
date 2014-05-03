<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.cmdbuild.auth.user.OperationUser"%>
<%@ page import="org.cmdbuild.services.SessionVars"%>
<%@ page import="org.cmdbuild.services.auth.User"%>
<%@ page import="org.cmdbuild.servlets.json.Login"%>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils"%>
<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%
	final SessionVars sessionVars = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class);
	final String lang = sessionVars.getLanguage();
	final OperationUser operationUser = sessionVars.getUser();
	final String extVersion = "4.2.0";
%>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="icon" href="images/favicon.ico" />

		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/ext-all.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/Notification.js"></script>

		<!-- 1. Main script -->
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Ajax.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Msg.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/PopupWindow.js"></script>

		<script type="text/javascript" src="javascripts/cmdbuild/core/proxy/CMProxyConstants.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/proxy/CMProxyUrlIndex.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/proxy/CMProxy.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/proxy/CMProxySetup.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/proxy/CMProxyConfiguration.js"></script>

		<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMIconCombo.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/LanguageCombo.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/model/CMSetupModels.js"></script>

		<!-- 2. Translations -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/locale/ext-lang-<%= lang %>.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

		<script type="text/javascript">
			Ext.ns('CMDBuild.Runtime'); // runtime configurations
			<%if (!operationUser.isValid() && !operationUser.getAuthenticatedUser().isAnonymous()) {%>
				CMDBuild.Runtime.Username = '<%=operationUser.getAuthenticatedUser().getUsername()%>';
				CMDBuild.Runtime.Groups =<%=Login.serializeGroupForLogin(operationUser.getAuthenticatedUser().getGroupNames())%>;
			<%}%>
			Ext.onReady(function() {
				CMDBuild.LoginPanel.buildAfterRequest();
			});
		</script>

		<!-- 3. Login script -->
		<script type="text/javascript" src="javascripts/cmdbuild/login.js"></script>
		<title>CMDBuild</title>
	</head>
	<body>
		<div id="header">
			<img alt="CMDBuild logo" src="images/logo.jpg" />
			<div id="header_po">Open Source Configuration and Management Database</div>
		</div>
		<div id="login_box_wrap">
			<div id="login_box"></div>
		</div>
		<div id="release_box">
			<span class="x-panel-header-text-default">CMDBuild <tr:translation key="release"/></span>
		</div>
	</body>
</html>