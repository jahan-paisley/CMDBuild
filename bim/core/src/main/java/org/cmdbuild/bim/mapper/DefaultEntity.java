package org.cmdbuild.bim.mapper;

import java.util.Map;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;

import com.google.common.collect.Maps;

public class DefaultEntity implements Entity {

	private Map<String, Attribute> attributesMap;
	private final String typeName;
	private final String key;

	private DefaultEntity(String typeName, String key) {
		this.key = key;
		this.typeName = typeName;
		this.attributesMap = Maps.newHashMap();
	}
	
	
	public static DefaultEntity withTypeAndKey(final String typeName, final String key){
		return new DefaultEntity(typeName,key);
	}

	@Override
	public boolean isValid() {
		return key != null && !key.isEmpty();
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return attributesMap;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public Attribute getAttributeByName(String attributeName) {
		return Attribute.class.cast(attributesMap.containsKey(attributeName) ? attributesMap.get(attributeName)
				: Attribute.NULL_ATTRIBUTE);
	}

	public void addAttribute(Attribute attribute) {
		attributesMap.put(attribute.getName(), attribute);
	}

	@Override
	public String toString() {
		return typeName + ": " + getKey();
	}

}
