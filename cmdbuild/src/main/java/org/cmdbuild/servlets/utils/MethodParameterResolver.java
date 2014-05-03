package org.cmdbuild.servlets.utils;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cmdbuild.logger.Log;
import org.json.JSONObject;

public class MethodParameterResolver {

	// TODO: polish code..it's pretty messy
	private enum ParameterAnnotation {
		SESSION(Session.class) {
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(final Annotation arg0, final Class arg1, final HttpServletRequest arg2) {
				return arg2.getSession().getAttribute(((Session) arg0).value());
			}

			@Override
			boolean isRequired(final Annotation arg0) {
				return ((Session) arg0).required();
			}
		},
		REQUEST(Request.class) {
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(final Annotation arg0, final Class arg1, final HttpServletRequest arg2) {
				return arg2.getAttribute(((Request) arg0).value());
			}

			@Override
			boolean isRequired(final Annotation arg0) {
				return ((Request) arg0).required();
			}
		},
		PARAMETER(Parameter.class) {
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(final Annotation annot, final Class type, final HttpServletRequest request) {
				final String key = ((Parameter) annot).value();

				if (type.isArray()) {
					final Class realType = type.getComponentType();
					final String[] values = getParamValues(request, key);
					if (values == null) {
						Log.JSONRPC.debug(key + " array was not found!");
						return null;
					}
					return ParameterTransformer.getInstance().safeArrayGeneralTransform(realType, request, key, values);
				} else {
					return ParameterTransformer.getInstance().safeTransform(type, request, key,
							getParamValue(request, key));
				}
			}

			@Override
			boolean isRequired(final Annotation arg0) {
				return ((Parameter) arg0).required();
			}
		},
		URIPARAM(URIParameter.class) { // REST ONLY
			@SuppressWarnings("unchecked")
			@Override
			Object getObj(final Annotation annot, final Class type, final HttpServletRequest request) {
				final int index = ((URIParameter) annot).value();
				final String[] paramValues = getStrippedPath(request);
				String out = null;
				if (index < paramValues.length) {
					out = paramValues[index];
				} else {
					out = "";
				}
				return out;
			}

			@Override
			boolean isRequired(final Annotation arg0) {
				return false;
			}
		};
		Class<? extends Annotation> annot;

		private ParameterAnnotation(final Class<? extends Annotation> annot) {
			this.annot = annot;
		}

		@SuppressWarnings("unchecked")
		abstract Object getObj(Annotation annot, Class type, HttpServletRequest req);

		abstract boolean isRequired(Annotation annot);

		static boolean hasAnnotation(final Annotation[] annots) {
			for (final Annotation a : annots) {
				for (final ParameterAnnotation pa : values()) {
					if (pa.annot.equals(a.annotationType())) {
						return true;
					}
				}
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		static Object resolve(final Annotation[] annots, final Class type, final HttpServletRequest req)
				throws Exception {
			if (!hasAnnotation(annots)) {
				return null;
			}
			for (final Annotation a : annots) {
				for (final ParameterAnnotation pa : values()) {
					if (pa.annot.equals(a.annotationType())) {
						final Object out = pa.getObj(a, type, req);
						if (out == null && pa.isRequired(a)) {
							throw new IllegalArgumentException("Required parameter \"" + ((Parameter) a).value()
									+ "\" not found!");
						} else {
							return out;
						}
					}
				}
			}
			return null;
		}
	}

	public static final String MultipartRequest = "_multipartParsedParams";
	public static final String RequestPathElements = "_restPathElements";

	private static MethodParameterResolver instance = null;

	public static MethodParameterResolver getInstance() {
		if (instance == null) {
			instance = new MethodParameterResolver();
		}
		return instance;
	}

	ParameterBuilder<JSONObject> bcpj = new ParameterBuilder<JSONObject>() {
		@Override
		public JSONObject build(final HttpServletRequest r, final OverrideKeys ignored) {
			return new JSONObject();
		};

		@Override
		public Class<JSONObject> getBindedClass() {
			return JSONObject.class;
		}
	};
	@SuppressWarnings("unchecked")
	ParameterBuilder<Map> bcpm = new ParameterBuilder<Map>() {
		@Override
		public Map build(final HttpServletRequest r, final OverrideKeys ignored) {
			return buildParametersMap(r);
		}

		@Override
		public Class<Map> getBindedClass() {
			return Map.class;
		}
	};

	@SuppressWarnings("unchecked")
	Map<Class, ParameterBuilder> builders;

	@SuppressWarnings("unchecked")
	private MethodParameterResolver() {
		builders = new HashMap();
		builders.put(JSONObject.class, this.bcpj);
		builders.put(Map.class, this.bcpm);
	}

	public <T> void putAutoloadParameter(final ParameterBuilder<T> prm) {
		builders.put(prm.getBindedClass(), prm);
	}

	public static final String parseKey(String key, final HttpServletRequest req) {
		if (key.indexOf('{') < 0) {
			return key;
		}
		final Pattern varPattern = Pattern.compile("\\{([A-Za-z0-9_]*)\\}");
		final Matcher varMatcher = varPattern.matcher(key);
		while (varMatcher.find()) {
			final String varName = varMatcher.group(1);
			final String value = getParamValue(req, varName);
			key = varMatcher.replaceFirst(value);
			varMatcher.reset(key);
		}
		return key;
	}

	/**
	 * Resolve method parameters. Flow: 1) HttpServlet{Request,Response} 2)
	 * binded class 3) -- custom if has constructor (User), (Role), (User,Role)
	 * or (Role,User) 4) annotation
	 * Session,Request,Parameter,URIParameter,OutSimpleXML
	 * 
	 * @param types
	 * @param paramsAnnots
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Object[] resolve(final Class[] types, final Annotation[][] paramsAnnots, final HttpServletRequest request,
			final HttpServletResponse response) throws Exception {
		final Object[] out = new Object[types.length];

		setupMultipart(request);

		for (int i = 0; i < types.length; i++) {
			final Class cls = types[i];
			final Annotation[] annots = paramsAnnots[i];

			if (annots.length == 0 || !ParameterAnnotation.hasAnnotation(annots)) {
				if (types[i].equals(HttpServletRequest.class)) {
					out[i] = request;
				} else if (types[i].equals(HttpServletResponse.class)) {
					out[i] = response;
				} else {
					OverrideKeys overrides = null;
					if (annots.length > 0) {
						for (final Annotation ann : annots) {
							if (ann.annotationType().equals(OverrideKeys.class)) {
								overrides = (OverrideKeys) ann;
								break;
							}
						}
					}
					out[i] = getObjectIfBinded(types[i], request, overrides);
				}
			} else {
				out[i] = ParameterAnnotation.resolve(annots, cls, request);// resolve(cls,
																			// annots,
																			// request);
			}

		}

		return out;
	}

	@SuppressWarnings("unchecked")
	private void setupMultipart(final HttpServletRequest request) throws FileUploadException {
		final List<FileItem> items;
		if (isMultipart(request)) {
			final FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			final ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			items = upload.parseRequest(request);
		} else {
			items = new ArrayList<FileItem>();
		}
		request.setAttribute(MultipartRequest, items);
	}

	@SuppressWarnings("unchecked")
	public static String getParamValue(final HttpServletRequest request, final String key) {
		if (!isMultipart(request)) {
			return request.getParameter(key);
		} else {
			for (final FileItem item : (List<FileItem>) request.getAttribute(MultipartRequest)) {
				if (item.isFormField() && item.getFieldName().equals(key)) {
					try {
						return item.getString("utf8");
					} catch (final UnsupportedEncodingException e) {
						Log.JSONRPC.error("Wrong encoding for parameter " + key);
						return "";
					}
				}
			}
		}
		return null;
	}

	// UNTESTED
	@SuppressWarnings("unchecked")
	public static String[] getParamValues(final HttpServletRequest request, final String key) {
		if (!isMultipart(request)) {
			return request.getParameterValues(key);
		} else {
			final List<String> values = new ArrayList();
			for (final FileItem item : (List<FileItem>) request.getAttribute(MultipartRequest)) {
				if (item.isFormField() && item.getFieldName().equals(key)) {
					values.add(item.getString());
				}
			}
			if (values.size() > 0) {
				return values.toArray(new String[] {});
			}
		}
		return null;
	}

	public static boolean isMultipart(final HttpServletRequest request) {
		return ServletFileUpload.isMultipartContent(request);
	}

	@SuppressWarnings("unchecked")
	private Object getObjectIfBinded(final Class type, final HttpServletRequest req, final OverrideKeys overrides)
			throws Exception {
		final ParameterBuilder bcp = builders.get(type);
		if (bcp != null) {
			return bcp.build(req, overrides);
		}
		return null;
	}

	private static Map<String, String> buildParametersMap(final HttpServletRequest req) {
		final Map<String, String> out = new HashMap<String, String>();
		for (final Object okey : req.getParameterMap().keySet()) {
			final String k = (String) okey;
			final String v = req.getParameter(k);
			out.put(k, v);
		}
		return out;
	}

	public static String[] getStrippedPath(final HttpServletRequest req) {
		if (null == req.getAttribute(RequestPathElements)) {
			final String reqUri = req.getPathInfo();
			final String[] out = reqUri.substring(1).split("/");
			req.setAttribute(RequestPathElements, out);
		}
		return (String[]) req.getAttribute(RequestPathElements);
	}

}
