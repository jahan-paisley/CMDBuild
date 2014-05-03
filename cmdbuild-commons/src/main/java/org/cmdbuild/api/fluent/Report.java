package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class Report {

	private final String title;
	private final String format;
	private final Map<String, Object> parameters;

	public Report(final String title, final String format) {
		this.title = title;
		this.format = format;
		parameters = new HashMap<String, Object>();
	}

	public String getTitle() {
		return title;
	}

	public String getFormat() {
		return format;
	}

	public Map<String, Object> getParameters() {
		return unmodifiableMap(parameters);
	}

	public Object get(final String name) {
		return parameters.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(final String name, final Class<T> clazz) {
		return (T) get(name);
	}

	void set(final String name, final Object value) {
		parameters.put(name, value);
	}

}
