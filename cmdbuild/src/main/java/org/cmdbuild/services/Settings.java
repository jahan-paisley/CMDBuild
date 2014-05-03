package org.cmdbuild.services;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdbuild.config.DefaultProperties;
import org.cmdbuild.utils.StringUtils;

public class Settings {
	
	private static Settings instance;

	private Map<String, DefaultProperties> _map;
	
	private String rootPath;

	private Settings(){
		_map = new LinkedHashMap<String, DefaultProperties>();
	}

	public static Settings getInstance()
	{
		if (instance == null)
			instance = new Settings();

		return instance; 
	}
	
	public void load(String key, String file) throws IOException {
		DefaultProperties properties = getModule(key);
		properties.load(file);
		_map.put(key, properties);
	}

	private DefaultProperties createPropertiesClass(String key) {
		DefaultProperties properties;
		try {
			String propertiesClass = "org.cmdbuild.config." + StringUtils.ucFirst(key) + "Properties";
			properties = (DefaultProperties)(Class.forName(propertiesClass).newInstance());
		} catch (Throwable e) {
			properties = new DefaultProperties();
		}
		return properties;
	}

	public void write(String key) throws IOException {
		if(_map.containsKey(key))
			getModule(key).store();
	}

	public DefaultProperties getModule(String key) {
		DefaultProperties prop = _map.get(key);
		if (prop == null) {
			prop = createPropertiesClass(key);
			_map.put(key, prop);
		}
		return prop;
	}
	
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return rootPath;
	}
}
