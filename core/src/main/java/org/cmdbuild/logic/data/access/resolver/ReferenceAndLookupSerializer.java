package org.cmdbuild.logic.data.access.resolver;

import java.util.Map;

import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.data.store.lookup.Lookup;

import com.google.common.collect.Maps;

public class ReferenceAndLookupSerializer<T extends CMEntry> extends AbstractSerializer<T> {

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		// final CardReference cardReference =
		// attributeType.convertValue(rawValue);
		// setAttribute(attributeName, idAndDescription(cardReference.getId(),
		// cardReference.getDescription()));
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		// final CardReference cardReference =
		// attributeType.convertValue(rawValue);
		// final Lookup lookup = lookupStore.read(Lookup.newInstance() //
		// .withId(cardReference.getId()) //
		// .build());
		// if (lookup != null) {
		// setAttribute(attributeName, idAndDescription(lookup.getId(),
		// descriptionOf(lookup)));
		// } else {
		// setAttribute(attributeName, idAndDescription(null,
		// StringUtils.EMPTY));
		// }
	}

	private String descriptionOf(final Lookup lookup) {
		final String concatFormat = "%s - %s";
		String description = lookup.description;
		final Lookup parent = lookup.parent;
		if (parent != null) {
			description = String.format(concatFormat, descriptionOf(parent), description);
		}
		return description;
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		// final CardReference cardReference =
		// attributeType.convertValue(rawValue);
		// setAttribute(attributeName, idAndDescription(cardReference.getId(),
		// cardReference.getDescription()));
	}

	private Map<String, Object> idAndDescription(final Long id, final String description) {
		final Map<String, Object> value = Maps.newHashMap();
		value.put("id", id);
		value.put("description", description);
		return value;
	}

}
