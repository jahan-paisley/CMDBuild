package org.cmdbuild.dao.view.user;

import static com.google.common.collect.FluentIterable.from;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class UserCard implements CMCard {

	private final CMCard inner;
	private final UserClass userClass;
	private final Map<String, Object> allValues;

	static UserCard newInstance(final UserDataView view, final CMCard inner) {
		final UserClass userClass = UserClass.newInstance(view, inner.getType());
		if (userClass == null) {
			/**
			 * It may happen if the user does not have privileges to read/write
			 * the class
			 */
			return null;
		}
		return new UserCard(userClass, inner);
	}

	private UserCard(final UserClass userClass, final CMCard inner) {
		this.inner = inner;
		this.userClass = userClass;
		this.allValues = Maps.newHashMap();
		for (final Entry<String, Object> entry : inner.getAllValues()) {
			final String name = entry.getKey();
			final CMAttribute attribute = userClass.getAttribute(name);
			if (attribute == null) {
				continue;
			}
			final Object value = entry.getValue();
			allValues.put(name, value);
		}
	}

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getUser() {
		return inner.getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return inner.getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return inner.getEndDate();
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		return allValues.entrySet();
	}

	@Override
	public Object get(final String key) {
		return allValues.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return requiredType.cast(get(key));
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return from(getAllValues()) //
				.filter(new Predicate<Map.Entry<String, Object>>() {
					@Override
					public boolean apply(final Entry<String, Object> input) {
						final String name = input.getKey();
						final UserAttribute attribute = userClass.getAttribute(name);
						return !attribute.isSystem();
					}
				});
	}

	@Override
	public UserClass getType() {
		return userClass;
	}

	@Override
	public Object getCode() {
		return inner.getCode();
	}

	@Override
	public Object getDescription() {
		return inner.getDescription();
	}

}
