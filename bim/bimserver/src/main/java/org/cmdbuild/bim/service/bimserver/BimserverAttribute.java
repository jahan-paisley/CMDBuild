package org.cmdbuild.bim.service.bimserver;

import org.apache.commons.lang3.StringUtils;
import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.cmdbuild.bim.model.Attribute;

public abstract class BimserverAttribute implements Attribute {

	private final SDataValue datavalue;

	public BimserverAttribute(final SDataValue datavalue) {
		this.datavalue = datavalue;
	}

	protected SDataValue getDatavalue() {
		return datavalue;
	}

	@Override
	public String getName() {
		return datavalue.getFieldName();
	}

	@Override
	public boolean isValid() {
		return datavalue != null;
	}

	@Override
	public String getValue() {
		String value = StringUtils.EMPTY;
		if (datavalue instanceof SSimpleDataValue) {
			value = ((SSimpleDataValue) datavalue).getStringValue();
		} else if (datavalue instanceof SReferenceDataValue) {
			value = SReferenceDataValue.class.cast(datavalue).getGuid();
		} else {
			throw new UnsupportedOperationException("Not supported");
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException("Not supported");
	}
}
