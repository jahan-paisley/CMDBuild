package org.cmdbuild.data.converter;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Code;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.data.store.Groupable;

public class MetadataGroupable extends MetadataStoreStuff implements Groupable {

	public static MetadataGroupable of(final CMAttribute attribute) {
		return new MetadataGroupable(attribute);
	}

	private MetadataGroupable(final CMAttribute attribute) {
		super(attribute);
	}

	@Override
	public String getGroupAttributeName() {
		return Code.getDBName();
	}

	@Override
	public Object getGroupAttributeValue() {
		return groupAttributeValue;
	}

}
