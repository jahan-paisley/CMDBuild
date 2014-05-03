package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.AttributeFactory;

public class BimserverAttributeFactory implements AttributeFactory {

	private final SDataValue datavalue;

	protected BimserverAttributeFactory(final SDataValue datavalue) {
		this.datavalue = datavalue;
	}

	@Override
	public Attribute create() {
		Attribute attribute = Attribute.NULL_ATTRIBUTE;
		if (datavalue instanceof SSimpleDataValue) {
			attribute = new BimserverSimpleAttribute((SSimpleDataValue) datavalue);
		} else if (datavalue instanceof SListDataValue) {
			attribute = new BimserverListAttribute((SListDataValue) datavalue);
		} else if (datavalue instanceof SReferenceDataValue) {
			attribute = new BimserverReferenceAttribute((SReferenceDataValue) datavalue);
		}
		return attribute;
	}

}
