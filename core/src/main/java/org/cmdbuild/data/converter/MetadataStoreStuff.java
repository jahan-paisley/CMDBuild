package org.cmdbuild.data.converter;

import static java.lang.String.format;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

class MetadataStoreStuff {

	protected final String groupAttributeValue;

	protected MetadataStoreStuff(final CMAttribute attribute) {
		final StringBuilder sb = new StringBuilder();
		final CMEntryType owner = attribute.getOwner();
		if (owner != null) {
			final CMIdentifier identifier = owner.getIdentifier();
			if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
				sb.append(format("%s.", identifier.getNameSpace()));
			}
			sb.append(format("%s.", identifier.getLocalName()));
			sb.append(attribute.getName());
		}

		this.groupAttributeValue = sb.toString();
	}

}