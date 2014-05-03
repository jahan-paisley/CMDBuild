package org.cmdbuild.services;

import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.json.JSONBase.JSONExported;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import com.google.classpath.ResourceFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class JSONDispatcherService {

	private static final long serialVersionUID = 1L;
	String baseClassPath = "org/cmdbuild/servlets/json"; // TODO: Put these as a servlet parameters

	private static JSONDispatcherService instance;
	
	public class MethodInfo {
		Method method;
		@SuppressWarnings("rawtypes")
		Class[] paramClasses;
		Annotation[][] paramsAnnotations;
		
		JSONExported annotation;
		
		public MethodInfo(Method m) {
			this.method = m;
			this.annotation = (JSONExported)m.getAnnotation(JSONExported.class);
			this.paramClasses = m.getParameterTypes();
			this.paramsAnnotations = m.getParameterAnnotations();
		}

		public Method getMethod() {
			return method;
		}
		@SuppressWarnings("rawtypes")
		public Class[] getParamClasses() {
			return paramClasses;
		}
		public Annotation[][] getParamsAnnotations() {
			return paramsAnnotations;
		}
		public JSONExported getMethodAnnotation() {
			return annotation;
		}
		public String toString() {
			return method.getDeclaringClass() + "." + method.getName();
		}
	}

	private HashMap<String, MethodInfo> jsonMethodsMapping = new HashMap<String, MethodInfo>();

	private JSONDispatcherService() {
		buildUrlMap();
	}

	public void reload() {
		jsonMethodsMapping.clear();
		buildUrlMap();
	}

	private void buildUrlMap() {
		for (Class<?> serviceClass : findExportableClasses()) {
			mapExportableMethodsFor(serviceClass);
		}
	}

	private void mapExportableMethodsFor(Class<?> serviceClass) {
		for (Method method : serviceClass.getMethods()) {
			if (method.isAnnotationPresent(JSONExported.class)) {
				String urlName = getUrlMappingFor(method);
				if (jsonMethodsMapping.put(urlName, new MethodInfo(method)) != null) {
					Log.JSONRPC.warn(urlName + " already mapped");
				}
			}
		}
	}

	private String getUrlMappingFor(Method method) {
		String completeMethodName = method.getDeclaringClass().getCanonicalName()+"."+method.getName();
		String urlName = completeMethodName.substring(baseClassPath.length()).toLowerCase().replace(".", "/");
		Log.JSONRPC.debug(completeMethodName + " mapped to " + urlName);
		return urlName;
	}

	public static JSONDispatcherService getInstance()
	{
		if (instance == null)
			instance = new JSONDispatcherService();

		return instance; 
	}

	public MethodInfo getMethodInfoFromURL(String url) {
		return jsonMethodsMapping.get(url);
	}

	protected List<Class<?>> findExportableClasses() {
		List<Class<?>> classes = new LinkedList<Class<?>>();
		ClassPath classPath = getPlausibleClassPath();
		ResourceFilter filter = new RegExpResourceFilter(
				RegExpResourceFilter.ANY,
				RegExpResourceFilter.ENDS_WITH_CLASS);
		for (String classFileName : classPath.findResources(baseClassPath, filter)) {
			String className = classNameFrom(classFileName);
			try {
				classes.add(Class.forName(className));
			} catch(ClassNotFoundException e) {
				Log.JSONRPC.warn("Can't load class " + className, e);
			}
		}
		return classes;
	}

	private ClassPath getPlausibleClassPath() {
		ClassPathFactory factory = new ClassPathFactory();
		ClassPath classPath;
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) contextClassLoader;
			classPath = factory.createFromPaths(getUrlStrings(urlClassLoader));
		}  else {
            LinkedList<String> paths= new LinkedList<String>();
            try {
                Enumeration<URL> urls= contextClassLoader.getResources("/");
                while(urls.hasMoreElements())
                {
                    URL url = urls.nextElement();
                    URL path= JBoss7ClasspathURLConverterImpl.convert(url);
                    paths.add(path.getFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            classPath= factory.createFromPaths(paths.toArray(new String[paths.size()]));
        }
//        else {
//            Log.JSONRPC.warn("Classes not loaded from URLs: using JVM classpath");
//			classPath = factory.createFromJVM();
//        }
		return classPath;
	}

	private String[] getUrlStrings(URLClassLoader urlClassLoader) {
		URL[] classPathUrls = urlClassLoader.getURLs();
		String[] classPathStrings = new String[classPathUrls.length];
		for (int i=0; i<classPathUrls.length; ++i) {
			classPathStrings[i] = classPathUrls[i].getFile();
		}
		return classPathStrings;
	}

	private String classNameFrom(String classFileName) {
		return classFileName.substring(0, classFileName.length()-6).replace("/", ".");
	}
}
