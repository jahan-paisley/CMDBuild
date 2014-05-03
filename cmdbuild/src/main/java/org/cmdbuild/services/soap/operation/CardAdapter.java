package org.cmdbuild.services.soap.operation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import net.sf.jasperreports.engine.util.ObjectUtils;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Card;

public class CardAdapter {

	private final CMDataView dataView;
	private final LookupStore lookupStore;

	public CardAdapter(final CMDataView dataView, final LookupStore lookupStore) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	public void resolveAttributes(final Card card) {
		final CMEntryType entryType = dataView.findClass(card.getClassName());
		for (final Attribute attribute : card.getAttributeList()) {
			final String name = attribute.getName();
			final CMAttributeType<?> attributeType = entryType.getAttribute(name).getType();
			attributeType.accept(new NullAttributeTypeVisitor() {

				@Override
				public void visit(final LookupAttributeType attributeType) {
					final String value = attribute.getValue();
					final String lookupTypeName = attributeType.getLookupTypeName();
					if (isNotBlank(value) && isNumeric(value)) {
						if (existsLookup(lookupTypeName, Long.parseLong(value))) {
							attribute.setValue(value);
						}
					} else {
						final Iterable<Lookup> lookupList = lookupStore.list();
						for (final Lookup lookup : lookupList) {
							if (lookup.active && //
									lookup.type.name.equals(lookupTypeName) && //
									lookup.description != null && //
									ObjectUtils.equals(lookup.description, value)) {
								attribute.setValue(lookup.getId().toString());
								break;
							}
						}
					}
				}

				private boolean existsLookup(final String lookupTypeName, final Long lookupId) {
					final Iterable<Lookup> lookupList = lookupStore.list();
					for (final Lookup lookup : lookupList) {
						if (lookup.type.name.equals(lookupTypeName) && lookup.getId().equals(lookupId)) {
							return true;
						}
					}
					return false;
				}

			});
		}
	}

}