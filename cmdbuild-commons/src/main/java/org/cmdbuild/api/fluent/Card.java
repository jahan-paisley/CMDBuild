package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Card extends CardDescriptor {

	private final Map<String, Object> attributes;

	Card(final String className, final Integer id) {
		super(className, id);
		attributes = new HashMap<String, Object>();
	}

	public boolean has(final String name) {
		return attributes.containsKey(name);
	}

	public boolean hasAttribute(final String name) {
		return has(name);
	}

	public Set<String> getAttributeNames() {
		return unmodifiableSet(attributes.keySet());
	}

	public Map<String, Object> getAttributes() {
		return unmodifiableMap(attributes);
	}

	public String getCode() {
		return get(CODE_ATTRIBUTE, String.class);
	}

	public String getDescription() {
		return get(DESCRIPTION_ATTRIBUTE, String.class);
	}

	public Object get(final String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	private <T> T get(final String name, final Class<T> clazz) {
		return (T) get(name);
	}

	void set(final String name, final Object value) {
		attributes.put(name, value);
	}

	void setCode(final String value) {
		set(CODE_ATTRIBUTE, value);
	}

	void setDescription(final String value) {
		set(DESCRIPTION_ATTRIBUTE, value);
	}

}