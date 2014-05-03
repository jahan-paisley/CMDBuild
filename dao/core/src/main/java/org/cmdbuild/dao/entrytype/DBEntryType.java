package org.cmdbuild.dao.entrytype;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.activeOnes;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.DBTypeObject;
import org.cmdbuild.dao.Metadata;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public abstract class DBEntryType extends DBTypeObject implements CMEntryType {

	public static class EntryTypeMetadata extends Metadata {

		protected static final String BASE_NS = "system.entrytype.";

		public static final String ACTIVE = BASE_NS + "active";
		public static final String DESCRIPTION = BASE_NS + "description";
		public static final String MODE = BASE_NS + "mode";
		public static final String HOLD_HISTORY = BASE_NS + "history";
		public static final String USER_STOPPABLE = BASE_NS + "stoppable";

		final String getDescription() {
			return get(DESCRIPTION);
		}

		final void setDescription(final String description) {
			put(DESCRIPTION, description);
		}

		final boolean isActive() {
			final String value = get(ACTIVE);
			return Boolean.parseBoolean((value == null) ? Boolean.TRUE.toString() : value);
		}

		final boolean isSystem() {
			return "reserved".equals(get(MODE)) || isSystemButUsable();
		}

		final boolean isBaseClass() {
			return "baseclass".equals(get(MODE));
		}

		public boolean isSystemButUsable() {
			// FIXME so bad, do it better
			return get(MODE).startsWith("sys");
		}

	}

	private static class NonSystemAttributes implements Predicate<DBAttribute> {

		@Override
		public boolean apply(final DBAttribute input) {
			return !input.isSystem();
		}

	}

	private static final NonSystemAttributes NON_SYSTEM_ATTRIBUTES = new NonSystemAttributes();

	private static final Function<DBAttribute, String> GET_ATTRIBUTE_NAME = new Function<DBAttribute, String>() {

		@Override
		public String apply(final DBAttribute input) {
			return input.getName();
		}

	};

	private final Map<String, DBAttribute> attributes;

	protected DBEntryType(final CMIdentifier identifier, final Long id, final List<DBAttribute> attributes) {
		super(identifier, id);
		this.attributes = newLinkedHashMap(uniqueIndex(attributes, GET_ATTRIBUTE_NAME));
		addAllAttributes(attributes);
	}

	protected abstract EntryTypeMetadata meta();

	/*
	 * CMEntryType overrides
	 */

	@Override
	public String getDescription() {
		return meta().getDescription();
	}

	@Override
	public boolean isActive() {
		return meta().isActive();
	}

	@Override
	public boolean isSystem() {
		return meta().isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return meta().isSystemButUsable();
	}

	@Override
	public boolean isBaseClass() {
		return meta().isBaseClass();
	}

	@Override
	public Iterable<DBAttribute> getActiveAttributes() {
		return from(getAttributes()) //
				.filter(activeOnes());
	}

	@Override
	public Iterable<DBAttribute> getAttributes() {
		return from(getAllAttributes()) //
				.filter(NON_SYSTEM_ATTRIBUTES);
	}

	@Override
	public Iterable<DBAttribute> getAllAttributes() {
		return attributes.values();
	}

	@Override
	public DBAttribute getAttribute(final String name) {
		return attributes.get(name);
	}

	public void addAttribute(final DBAttribute attribute) {
		attribute.owner = this;
		attributes.put(attribute.getName(), attribute);
	}

	public void removeAttribute(final DBAttribute attribute) {
		if (attribute.owner == this) {
			attributes.remove(attribute.getName());
		}
	}

	private void addAllAttributes(final List<DBAttribute> attributes) {
		for (final DBAttribute attribute : attributes) {
			addAttribute(attribute);
		}
	}

	@Override
	public String getKeyAttributeName() {
		// TODO Mark it in the metadata!
		return "Id";
	}

}
