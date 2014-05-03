package org.cmdbuild.servlets.utils.builder;

import java.lang.reflect.ParameterizedType;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.OverrideKeys;
import org.cmdbuild.servlets.utils.ParameterBuilder;
import org.cmdbuild.servlets.utils.ParameterTransformer;

public abstract class AbstractParameterBuilder<T> implements ParameterBuilder<T> {
	
	public abstract T build(HttpServletRequest request) throws Exception;
	
	public T build(HttpServletRequest r, OverrideKeys overrides)
			throws Exception {
		r.setAttribute("overrides", overrides);
		return build(r);
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getBindedClass() {
		return (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	protected <V> V session(String key, HttpServletRequest req) {
		return (V)req.getSession().getAttribute(key);
	}
	
	@SuppressWarnings("unchecked")
	protected <V> V request(String key, HttpServletRequest req) {
		return (V)req.getAttribute(key);
	}
	
	@SuppressWarnings("unchecked")
	protected <V> V parameter( Class<V> type, String key, HttpServletRequest req ) {
		key = getRealKey(key,req);
		String v = MethodParameterResolver.getParamValue(req, key);
		if(type.equals(String.class)) return (V)v;
		return ParameterTransformer.getInstance().safeTransform(type, req, key, v);
	}
	
	private String getRealKey( String key, HttpServletRequest r ) {
		OverrideKeys ovrs = (OverrideKeys)r.getAttribute("overrides");
		if (ovrs != null) {
			int index = 0;
			for( String k : ovrs.key() ) {
				if(k.equals(key)) {
					return ovrs.newKey()[index];
				}
				index++;
			}
		}
		return key;
	}
	
	/**
	 * <V> can't be a primitive class, ie. Integer.TYPE, but it's wrapper class must bu used, ie. Integer.class
	 * @param <V>
	 * @param type
	 * @param key
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <V> V[] parameters( Class<V> type, String key, HttpServletRequest req ) {
		key = getRealKey( key,req );
		String[] vs = MethodParameterResolver.getParamValues(req, key);
		if(type.equals(String.class)) return (V[])vs;
		return ParameterTransformer.getInstance().safeArrayTransform(type, req, key, vs);
	}
	
	protected String parameter( String key, HttpServletRequest req ) {
		return MethodParameterResolver.getParamValue(req, key);
	}
}
