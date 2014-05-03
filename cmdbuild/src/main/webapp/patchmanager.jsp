<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.cmdbuild.services.SessionVars"%>
<%@ page import="org.cmdbuild.services.gis.GisDatabaseService"%>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils"%>

<%
	final SessionVars sessionVars = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class);
	final String lang = sessionVars.getLanguage();
	final String extVersion = "4.2.0";
%>

<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>CMDBuild - Patch Manager</title>

		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="icon" href="images/favicon.ico" />

		<!-- 0. ExtJS -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/ext-all.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/form/XCheckbox.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/Notification.js"></script>

		<!-- 1. Main script -->
		<script type="text/javascript">
			Ext.BLANK_IMAGE_URL = 'javascripts/ext-<%= extVersion %>/resources/images/default/s.gif';
		</script>

		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Msg.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/PopupWindow.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Ajax.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/form/FormOverride.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/form/CallbackPlugin.js"></script>

		<!-- 2. Translations -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/locale/ext-lang-<%= lang %>.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

		<script type="text/javascript">
			Ext.ns('CMDBuild.Config.cmdbuild');
			CMDBuild.Config.cmdbuild.language = '<%= lang %>';
		</script>

		<!-- 4. Modules -->
		<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/Buttons.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/model/CMPatchModel.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/patchmanager.js"></script>
	</head>
	<body>
		<div id="header">
			<img alt="CMDBuild logo" src="images/logo.jpg" />
			<div id="header_po">Open Source Configuration and Management Database</div>
		</div>

		<div id="grid"></div>

		<div id="footer">
			<div class="fl"><a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a></div>
			<div class="fc">credits: <a href="http://www.tecnoteca.com">Tecnoteca srl</a>, <a href="http://www.comune.udine.it">Comune di Udine</a>, <a href="http://www.cogitek.it">Cogitek srl</a></div>
			<div class="fr"><a href="http://www.tecnoteca.com" target="_blank">Copyright &copy; Tecnoteca srl</a></div>
		</div>
	</body>
</html>