package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.Maps.filterValues;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class LookupStorableConverter extends BaseStorableConverter<Lookup> {

	public static final String TABLE_NAME = "LookUp";

	private static final String CODE = "Code";
	private static final String DESCRIPTION = "Description";
	private static final String STATUS = "Status";
	private static final String NOTES = "Notes";
	private static final String TYPE = "Type";
	private static final String PARENT_TYPE = "ParentType";
	private static final String PARENT_ID = "ParentId";
	private static final String NUMBER = "Number";
	private static final String IS_DEFAULT = "IsDefault";

	private static final String A = "A";
	private static final String N = "N";

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public Lookup convert(final CMCard card) {
		return Lookup.newInstance() //
				.withId(card.getId()) //
				.withCode((String) card.getCode()) //
				.withDescription((String) card.getDescription()) //
				.withNotes(card.get(NOTES, String.class)) //
				.withType(LookupType.newInstance() //
						.withName(card.get(TYPE, String.class)) //
						.withParent(card.get(PARENT_TYPE, String.class))) //
				.withNumber(card.get(NUMBER, Integer.class)) //
				.withActiveStatus(A.equals(card.get(STATUS, String.class))) //
				.withDefaultStatus(card.get(IS_DEFAULT, Boolean.class)) //
				.withParentId(safeIntegerToLong(card.get(PARENT_ID, Integer.class), Long.class)) //
				.build();
	}

	private static Long safeIntegerToLong(final Integer from, final Class<Long> toClass) {
		return (from == null) ? null : Long.valueOf(from);
	}

	@Override
	public Map<String, Object> getValues(final Lookup storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(CODE, storable.code);
		values.put(DESCRIPTION, storable.description);
		values.put(NOTES, storable.notes);
		values.put(TYPE, storable.type.name);
		values.put(PARENT_TYPE, storable.type.parent);
		values.put(NUMBER, storable.number);
		values.put(STATUS, storable.active ? A : N);
		values.put(IS_DEFAULT, storable.isDefault);
		values.put(PARENT_ID, (storable.parentId != null && storable.parentId == 0) ? null : storable.parentId);
		return filterValues(values, new Predicate<Object>() {
			@Override
			public boolean apply(final Object input) {
				return (input != null);
			};
		});
	}

}
