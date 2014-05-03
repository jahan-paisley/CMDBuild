package org.cmdbuild.servlets.utils;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.utils.transformer.AbstractTransformer;

public class ParameterTransformer {
	
	static ParameterTransformer instance = null;
	public static ParameterTransformer getInstance() {
		if(instance == null){ instance = new ParameterTransformer(); }
		return instance;
	}
	
	static final Transformer<Boolean> booleanTransformer = new AbstractTransformer<Boolean>(){
		public Boolean transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Boolean.parseBoolean(value[0]);
		}
		@Override
		public Boolean defaultValue() {
			return false;
		}
	};
	static final Transformer<Integer> integerTransformer = new AbstractTransformer<Integer>() {
		public Integer transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Integer.parseInt(value[0]);
		}
		@Override
		public Integer defaultValue() {
			return 0;
		}
	};
	static final Transformer<Long> longTransformer = new AbstractTransformer<Long>() {
		public Long transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Long.parseLong(value[0]);
		}
		@Override
		public Long defaultValue() {
			return 0L;
		}
	};
	static final Transformer<Float> floatTransformer = new AbstractTransformer<Float>() {
		public Float transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Float.parseFloat(value[0]);
		}
		@Override
		public Float defaultValue() {
			return 0f;
		}
	};
	static final Transformer<Double> doubleTransformer = new AbstractTransformer<Double>() {
		public Double transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Double.valueOf(value[0]);
		}
		@Override
		public Double defaultValue() {
			return 0d;
		}
	};
	static final Transformer<Short> shortTransformer = new AbstractTransformer<Short>() {
		public Short transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Short.valueOf(value[0]);
		}
		@Override
		public Short defaultValue() {
			return 0;
		}
	};
	static final Transformer<Byte> byteTransformer = new AbstractTransformer<Byte>() {
		public Byte transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			return Byte.valueOf(value[0]);
		}
		@Override
		public Byte defaultValue() {
			return 0;
		}
	};
	static final Transformer<Character> charTransformer = new AbstractTransformer<Character>() {
		public Character transform(HttpServletRequest request,
				Object context, String... value)
				throws Exception {
			if (value[0].length() > 1) {
				throw new Exception(
						"charTransformer can transform only 1 char length strings!");
			}
			return value[0].charAt(0);
		}
		@Override
		public Character defaultValue() {
			return ' ';
		}
	};
	static final Transformer<String> stringTransformer = new AbstractTransformer<String>(){
		public String transform(HttpServletRequest request, Object context,
				String... value) throws Exception {
			return value[0];
		}
		@Override
		public String defaultValue() {
			return "";
		}
	};
	
	Map<Class<?>,Transformer<?>> mapping;
	
	@SuppressWarnings("unchecked")
	private ParameterTransformer() {
		this.mapping = new HashMap();
		
		//add default transformers
		addTransformer( stringTransformer, String.class );
		addTransformer( booleanTransformer,Boolean.TYPE,Boolean.class );
		addTransformer( integerTransformer,Integer.TYPE,Integer.class );
		addTransformer( longTransformer,Long.TYPE,Long.class );
		addTransformer( floatTransformer,Float.TYPE,Float.class );
		addTransformer( doubleTransformer,Double.TYPE,Double.class );
		addTransformer( shortTransformer,Short.TYPE,Short.class );
		addTransformer( byteTransformer,Byte.TYPE,Byte.class );
		addTransformer( charTransformer,Character.TYPE,Character.class );
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasTransformer( Class cls ) {
		return mapping.containsKey(cls);
	}
	
	//this is only useful for primitive types...
	@SuppressWarnings("unchecked")
	protected void addTransformer( Transformer transformer,Class...classes ) {
		for(Class cls : classes){
			if(!hasTransformer(cls)) {
				mapping.put(cls, transformer);
			}
		}
	}
	
	public <T> void addTransformer( Transformer<T> transformer ) {
		Class<T> cls = transformer.getTransformedClass();
		if(!hasTransformer(cls)) {
			mapping.put(cls, transformer);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T transform( Class<T> theCls, HttpServletRequest request, Object ctxt,String...value )
	throws Exception {
		Transformer<T> transformer = (Transformer<T>) mapping.get(theCls);
		if(transformer == null) {
			Log.CMDBUILD.error("Can not find transformer for class " + theCls);
			throw new Exception( "transformer for class " + theCls + "not found!" );
		}
		return transformer.transform(request, ctxt, value);
	}
	
	/**
	 * If an error occur, return the Transformer.defaultValue() instead of throwing an exception.
	 * @param <T>
	 * @param theCls
	 * @param request
	 * @param response
	 * @param ctxt
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T safeTransform( Class<T> theCls, HttpServletRequest request, Object ctxt,String...value ){
		try{
			return transform(theCls,request,ctxt,value);
		} catch(Exception e){
			return (T)mapping.get(theCls).defaultValue();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] safeArrayTransform( Class<T> theCls, HttpServletRequest request, Object ctxt, String...values ) {
		T[] out = (T[]) Array.newInstance(theCls, values.length);
		int index = 0;
		for(String value : values) {
			out[index] = safeTransform(theCls,request,ctxt,value);
			index++;
		}
		return out;
	}
	
	public <T> Object safeArrayGeneralTransform(Class<T> theCls, HttpServletRequest request, Object ctxt, String...values) {
		if(values == null){
			return null;
		}
		Object out = Array.newInstance(theCls, values.length);
		int index = 0;
		for(String value : values) {
			Array.set(out, index, safeTransform(theCls,request,ctxt,value));
			index++;
		}
		return out;
	}
}
