package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.cmdbuild.bim.service.ReferenceAttribute;

public class BimserverReferenceAttribute extends BimserverAttribute implements
		ReferenceAttribute {

	protected BimserverReferenceAttribute(final SReferenceDataValue value) {
		super(value);
	}

	@Override
	public String getGlobalId() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getGuid();
	}

	@Override
	public long getOid() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getOid();
	}

	@Override
	public String getTypeName() {
		final SReferenceDataValue referencedatavalue = (SReferenceDataValue) getDatavalue();
		return referencedatavalue.getTypeName();
	}

}
