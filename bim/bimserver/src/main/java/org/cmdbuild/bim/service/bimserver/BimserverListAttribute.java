package org.cmdbuild.bim.service.bimserver;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.service.ListAttribute;

public class BimserverListAttribute extends BimserverAttribute implements
		ListAttribute {

	protected BimserverListAttribute(final SListDataValue value) {
		super(value);
	}

	@Override
	public List<Attribute> getValues() {
		final List<SDataValue> datavalues = ((SListDataValue) getDatavalue())
				.getValues();
		final List<Attribute> values = new ArrayList<Attribute>();
		for (final SDataValue datavalue : datavalues) {
			if (datavalue instanceof SSimpleDataValue) {
				final Attribute attribute = new BimserverSimpleAttribute(
						(SSimpleDataValue) datavalue);
				values.add(attribute);
			} else if (datavalue instanceof SListDataValue) {
				final Attribute attribute = new BimserverListAttribute(
						(SListDataValue) datavalue);
				values.add(attribute);
			} else if (datavalue instanceof SReferenceDataValue) {
				final Attribute attribute = new BimserverReferenceAttribute(
						(SReferenceDataValue) datavalue);
				values.add(attribute);
			}
		}
		return values;
	}

}
