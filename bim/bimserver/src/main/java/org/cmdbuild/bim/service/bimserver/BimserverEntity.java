package org.cmdbuild.bim.service.bimserver;

import java.util.List;
import java.util.Map;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;

import com.google.common.collect.Maps;

public class BimserverEntity implements Entity {

	private final SDataObject bimserverDataObject;

	protected BimserverEntity(final SDataObject object) {
		this.bimserverDataObject = object;
	}

	@Override
	public boolean isValid() {
		return (bimserverDataObject != null);
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		final List<SDataValue> values = bimserverDataObject.getValues();
		final Map<String, Attribute> attributes = Maps.newHashMap();
		for (final SDataValue datavalue : values) {
			final BimserverAttributeFactory attributeFactory = new BimserverAttributeFactory(datavalue);
			final Attribute attribute = attributeFactory.create();
			attributes.put(datavalue.getFieldName(), attribute);
		}
		return attributes;
	}

	@Override
	public Attribute getAttributeByName(final String attributeName) {
		Attribute attribute = Attribute.NULL_ATTRIBUTE;
		Map<String, Attribute> attributes = getAttributes(); 
		
		if(attributes.containsKey(attributeName)){
			attribute = attributes.get(attributeName);
		}
		return attribute;
	}

	@Override
	public String getKey() {
		return (bimserverDataObject.getGuid() != null && !bimserverDataObject.getGuid().isEmpty()) ? bimserverDataObject.getGuid() : String.valueOf(bimserverDataObject.getOid());
	}

	public Long getOid() {
		return bimserverDataObject.getOid();
	}

	@Override
	public String getTypeName() {
		return bimserverDataObject.getType();
	}

	@Override
	public String toString() {
		return bimserverDataObject.getType() + " " + getKey();
	}

}
