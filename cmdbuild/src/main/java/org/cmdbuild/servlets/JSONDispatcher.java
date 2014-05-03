package org.cmdbuild.servlets;

import static org.cmdbuild.logic.auth.AuthenticationLogicUtils.assureAdmin;
import static org.cmdbuild.logic.auth.AuthenticationLogicUtils.isLoggedIn;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.JSONDispatcherService;
import org.cmdbuild.services.JSONDispatcherService.MethodInfo;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBase.Configuration;
import org.cmdbuild.servlets.json.JSONBase.MultipleException;
import org.cmdbuild.servlets.json.JSONBase.PartialFailureException;
import org.cmdbuild.servlets.json.JSONBase.SkipExtSuccess;
import org.cmdbuild.servlets.json.JSONBase.Unauthorized;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class JSONDispatcher extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Log.JSONRPC;

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
			ServletException {
		dispatch(request, response);
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
			ServletException {
		dispatch(request, response);
	}

	public void dispatch(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
			throws IOException, ServletException {
		httpResponse.setCharacterEncoding("UTF-8");
		final String url = getMethodUrl(httpRequest);
		final MethodInfo methodInfo = JSONDispatcherService.getInstance().getMethodInfoFromURL(url);
		if (methodInfo == null) {
			Log.JSONRPC.warn("Method not found for URL " + url);
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		try {
			try {
				if (methodInfo.getMethod().getAnnotation(Unauthorized.class) == null) {
					checkAuthentication(httpRequest);
				}
				final Admin adminAnnotation = methodInfo.getMethod().getAnnotation(Admin.class);
				if (adminAnnotation != null) {
					checkAdmin(httpRequest, adminAnnotation.value());
				}
				if (methodInfo.getMethod().getAnnotation(Configuration.class) != null) {
					checkUnconfigured();
				}

				final JSONBase targetClass = (JSONBase) methodInfo.getMethod().getDeclaringClass().newInstance();
				targetClass.init(httpRequest, httpResponse);

				final Class<?>[] types = methodInfo.getParamClasses();
				final Annotation[][] paramsAnnots = methodInfo.getParamsAnnotations();

				final Object[] params = MethodParameterResolver.getInstance().resolve(types, paramsAnnots, httpRequest,
						httpResponse);
				final Object methodResponse = methodInfo.getMethod().invoke(targetClass, params);

				writeResponse(methodInfo, methodResponse, httpRequest, httpResponse);
			} catch (final InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (final Throwable t) {
			logError(methodInfo, t);
			writeErrorMessage(methodInfo, t, httpRequest, httpResponse);
		}
	}

	private void logError(final MethodInfo methodInfo, final Throwable t) {
		if (Log.JSONRPC.isDebugEnabled()) {
			Log.JSONRPC.debug("Uncaught exception calling method " + methodInfo, t);
		} else {
			final StringBuffer message = new StringBuffer();
			message.append("A ").append(t.getClass().getCanonicalName()).append(" occurred calling method ")
					.append(methodInfo);
			if (t.getMessage() != null) {
				message.append(": ").append(t.getMessage());
			}
			Log.JSONRPC.error(message.toString());
		}
	}

	private void checkUnconfigured() {
		if (DatabaseProperties.getInstance().isConfigured()) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

	/*
	 * This method does not allow redirection
	 */
	private void checkAuthentication(final HttpServletRequest httpRequest) {
		try {
			if (isLoggedIn(httpRequest)) {
				return;
			}
		} catch (final Exception e) {
		}
		throw AuthExceptionType.AUTH_NOT_LOGGED_IN.createException();
	}

	private void checkAdmin(final HttpServletRequest httpRequest, final AdminAccess adminAccess) {
		assureAdmin(httpRequest, adminAccess);
	}

	private String getMethodUrl(final HttpServletRequest httpRequest) {
		String url = httpRequest.getPathInfo();
		// Legacy method call
		final String legacyMethod = httpRequest.getParameter("method");
		if (legacyMethod != null) {
			url += "/" + legacyMethod.toLowerCase();
			Log.JSONRPC.warn("Using legacy method specification for url " + url);
		} else {
			Log.JSONRPC.info("Calling url " + url);
		}
		if (Log.JSONRPC.isDebugEnabled()) {
			printRequestParameters(httpRequest);
		}
		return url;
	}

	@SuppressWarnings("unchecked")
	private void printRequestParameters(final HttpServletRequest httpRequest) {
		final Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		for (final String parameterName : parameterMap.keySet()) {
			if ("method".equals(parameterName)) {
				continue;
			}
			final String[] parameterValues = parameterMap.get(parameterName);
			String printableParameterValue;
			if (!Log.JSONRPC.isTraceEnabled() && parameterName.toLowerCase().contains("password")) {
				printableParameterValue = "***";
			} else {
				printableParameterValue = parameterValueToString(parameterValues);
			}
			Log.JSONRPC.debug(String.format("    parameter \"%s\": %s", parameterName, printableParameterValue));
		}
	}

	private String parameterValueToString(final String[] parameterValues) {
		String printValue;
		if (parameterValues.length == 1) {
			printValue = parameterValues[0];
		} else {
			final StringBuffer sb = new StringBuffer();
			sb.append("{\"");
			for (int i = 0; i < parameterValues.length; ++i) {
				if (i != 0) {
					sb.append("\", \"");
				}
				sb.append(parameterValues[i]);
			}
			sb.append("\"}");
			printValue = sb.toString();
		}
		return printValue;
	}

	private void writeResponse(final MethodInfo methodInfo, Object methodResponse,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws JSONException,
			IOException {
		methodResponse = addSuccessAndWarningsIfJSON(methodInfo, methodResponse);
		setContentType(methodInfo, methodResponse, httpRequest, httpResponse);
		writeResponseData(methodInfo, methodResponse, httpResponse);
	}

	private Object addSuccessAndWarningsIfJSON(final MethodInfo javaMethod, final Object methodResponse)
			throws JSONException {
		if (javaMethod.getMethod().getAnnotation(SkipExtSuccess.class) != null) {
			return methodResponse;
		}
		final Class<?> returnType = javaMethod.getMethod().getReturnType();
		if (Void.TYPE == returnType) {
			return addSuccessAndWarningsToJSONObject(new JSONObject());
		} else if (methodResponse instanceof JSONObject) {
			final JSONObject jres = (JSONObject) methodResponse;
			return addSuccessAndWarningsToJSONObject(jres);
		} else if (methodResponse instanceof JsonResponse) {
			return serializeJsonResponse(methodResponse);
		} else {
			return methodResponse;
		}
	}

	private Object serializeJsonResponse(final Object methodResponse) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(methodResponse);
		} catch (final Exception e) {
			logger.error("error mapping object", e);
			return "{\"success\":false}";
		}
	}

	private Object addSuccessAndWarningsToJSONObject(final JSONObject jres) throws JSONException {
		addRequestWarnings(jres);
		// Login.login() sets success to false instead of throwing the exception
		if (!jres.has("success")) {
			jres.put("success", true);
		}
		return jres;
	}

	private void setContentType(final MethodInfo methodInfo, final Object methodResponse,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
		if (MethodParameterResolver.isMultipart(httpRequest)) {
			/**
			 * Ext fake the ajax call with an iframe, and we need to tell the
			 * browser to not change the text (if not, the json response will be
			 * surrounded by a "pre" tag).
			 */
			httpResponse.setContentType("text/html");
		} else if (methodResponse instanceof DataHandler) {
			final DataHandler dh = (DataHandler) methodResponse;
			httpResponse.setContentType(dh.getContentType());
		} else if (methodResponse instanceof String) {
			httpResponse.setContentType("text/plain");
		} else {
			httpResponse.setContentType(methodInfo.getMethodAnnotation().contentType());
		}
	}

	private void writeResponseData(final MethodInfo methodInfo, final Object methodResponse,
			final HttpServletResponse httpResponse) throws IOException {
		if (methodResponse instanceof DataHandler) {
			final DataHandler dh = (DataHandler) methodResponse;
			httpResponse.setHeader("Content-Disposition", String.format("inline; filename=\"%s\";", dh.getName()));
			httpResponse.setHeader("Expires", "0");
			dh.writeTo(httpResponse.getOutputStream());
		} else if (methodResponse instanceof Document) {
			final XMLWriter writer = new XMLWriter(httpResponse.getWriter());
			writer.write(methodResponse);
		} else {
			httpResponse.getWriter().write(methodResponse.toString());
		}
	}

	private void writeErrorMessage(final MethodInfo methodInfo, final Throwable exception,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException {
		if (methodInfo == null) {
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		final Class<?> returnType = methodInfo.getMethod().getReturnType();
		if (DataHandler.class == returnType || methodInfo.getMethod().getAnnotation(SkipExtSuccess.class) != null) {
			if (exception instanceof NotFoundException) {
				httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else if (exception instanceof AuthException) {
				httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else {
				httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		} else {
			if (MethodParameterResolver.isMultipart(httpRequest)) {
				httpResponse.setContentType("text/html");
			} else {
				httpResponse.setContentType("application/json");
			}
			try {
				final JSONObject jsonOutput = getOutput(exception);
				addErrors(jsonOutput, exception);
				jsonOutput.put("success", false);
				httpResponse.getWriter().write(jsonOutput.toString());
			} catch (final JSONException e) {
				Log.CMDBUILD.error("Can't serialize the exception", e);
			}
		}
	}

	public JSONObject getOutput(final Throwable exception) {
		final JSONObject jsonOutput;
		if (exception instanceof PartialFailureException) {
			jsonOutput = ((PartialFailureException) exception).getPartialOutput();
		} else {
			jsonOutput = new JSONObject();
		}
		return jsonOutput;
	}

	private void addErrors(final JSONObject jsonOutput, Throwable exception) throws JSONException {
		if (exception instanceof PartialFailureException) {
			exception = ((PartialFailureException) exception).getOriginalException();
		}
		if (exception instanceof MultipleException) {
			final MultipleException me = (MultipleException) exception;
			jsonOutput.put("errors", serializeExceptionArray(me.getExceptions()));
		} else {
			jsonOutput.append("errors", serializeException(exception));
		}
	}

	private void addRequestWarnings(final JSONObject jsonOutput) throws JSONException {
		final List<? extends Throwable> warnings = applicationContext().getBean(RequestListener.class) //
				.getCurrentRequest().getWarnings();
		if (!warnings.isEmpty()) {
			jsonOutput.put("warnings", serializeExceptionArray(warnings));
		}
	}

	public JSONArray serializeExceptionArray(final Iterable<? extends Throwable> exceptions) throws JSONException {
		final JSONArray exceptionArray = new JSONArray();
		for (final Throwable t : exceptions) {
			exceptionArray.put(serializeException(t));
		}
		return exceptionArray;
	}

	static private JSONObject serializeException(final Throwable e) throws JSONException {
		final JSONObject exceptionJson = new JSONObject();
		if (e instanceof CMDBException) {
			final CMDBException ce = (CMDBException) e;
			exceptionJson.put("reason", ce.getExceptionTypeText());
			exceptionJson.put("reasonParameters",
					JSONDispatcher.serializeExceptionParameters(ce.getExceptionParameters()));
		}
		addStackTrace(exceptionJson, e);
		return exceptionJson;
	}

	static private JSONArray serializeExceptionParameters(final String[] exceptionParameters) {
		final JSONArray jsonParameters = new JSONArray();
		if (exceptionParameters != null) {
			for (int i = 0; i < exceptionParameters.length; ++i) {
				jsonParameters.put(exceptionParameters[i]);
			}
		}
		return jsonParameters;
	}

	private static void addStackTrace(final JSONObject exceptionJson, final Throwable t) throws JSONException {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		sw.flush();
		exceptionJson.put("stacktrace", sw.toString());
	}

}
