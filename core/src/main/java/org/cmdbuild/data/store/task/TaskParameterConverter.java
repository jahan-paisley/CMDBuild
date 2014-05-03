package org.cmdbuild.data.store.task;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.collect.Maps;

public class TaskParameterConverter extends BaseStorableConverter<TaskParameter> {

	private static final String CLASSNAME = "_TaskParameter";

	public static final String OWNER = "Owner";
	private static final String KEY = "Key";
	private static final String VALUE = "Value";

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public TaskParameter convert(final CMCard card) {
		return TaskParameter.newInstance() //
				.withId(card.getId()) //
				.withOwner(card.get(OWNER, Integer.class).longValue()) //
				.withKey(card.get(KEY, String.class)) //
				.withValue(card.get(VALUE, String.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final TaskParameter storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(OWNER, storable.getOwner());
		values.put(KEY, storable.getKey());
		values.put(VALUE, storable.getValue());
		return values;
	}

}
