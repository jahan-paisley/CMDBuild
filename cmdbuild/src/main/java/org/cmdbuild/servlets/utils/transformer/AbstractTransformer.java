package org.cmdbuild.servlets.utils.transformer;

import java.lang.reflect.ParameterizedType;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.servlets.utils.Transformer;


public abstract class AbstractTransformer<T> implements Transformer<T> {
	
	public T defaultValue() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getTransformedClass() {
		return (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	@SuppressWarnings("unchecked")
	protected <V> V session(HttpServletRequest r, String key){
		return (V)r.getSession().getAttribute(key);
	}
	
	@SuppressWarnings("unchecked")
	protected <V> V request(HttpServletRequest r, String key){
		return (V)r.getAttribute(key);
	}

}
