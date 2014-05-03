package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.cmdbuild.bim.service.SimpleAttribute;

public class BimserverSimpleAttribute extends BimserverAttribute implements
		SimpleAttribute {

	protected BimserverSimpleAttribute(final SSimpleDataValue datavalue) {
		super(datavalue);
	}

	@Override
	public String getStringValue() {
		final SSimpleDataValue dataValue = (SSimpleDataValue) getDatavalue();
		return dataValue.getStringValue();
	}

}
