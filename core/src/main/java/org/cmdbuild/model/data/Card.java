package org.cmdbuild.model.data;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.Storable;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class Card implements Storable {

	public static class CardBuilder implements Builder<Card> {

		private Long id;
		private CMClass type;
		private String className;
		private Long classId;
		private String classDescription;
		private DateTime begin;
		private DateTime end;
		private String user;
		private Map<String, Object> attributes = Maps.newHashMap();

		public CardBuilder clone(final Card card) {
			this.id = card.id;
			this.className = card.className;
			this.classDescription = card.classDescription;
			this.classId = card.classId;
			this.begin = card.begin;
			this.end = card.end;
			this.user = card.user;
			this.attributes = card.attributes;
			this.type = card.type;
			return this;
		}

		public CardBuilder() {
		}

		public CardBuilder(final CMClass type) {
			this.type = type;
			this.className = type.getName();
		}

		public CardBuilder withId(final Long value) {
			this.id = value;
			return this;
		}

		public CardBuilder withClassName(final String value) {
			this.className = value;
			return this;
		}

		public CardBuilder withClassId(final Long value) {
			this.classId = value;
			return this;
		}

		public CardBuilder withClassDescription(final String classDescription) {
			this.classDescription = classDescription;
			return this;
		}

		public CardBuilder withBeginDate(final DateTime value) {
			this.begin = value;
			return this;
		}

		public CardBuilder withEndDate(final DateTime value) {
			this.end = value;
			return this;
		}

		public CardBuilder withUser(final String value) {
			this.user = value;
			return this;
		}

		public CardBuilder withAttribute(final String key, final Object value) {
			this.attributes.put(key, value);
			return this;
		}

		public CardBuilder withAllAttributes(final Map<String, Object> values) {
			this.attributes.putAll(values);
			return this;
		}

		public CardBuilder withAllAttributes(final Iterable<Map.Entry<String, Object>> values) {
			for (final Map.Entry<String, Object> entry : values) {
				this.attributes.put(entry.getKey(), entry.getValue());
			}
			return this;
		}

		@Override
		public Card build() {
			Validate.isTrue(isNotBlank(className));
			return new Card(this);
		}

	}

	public static CardBuilder newInstance() {
		return new CardBuilder();
	}

	public static CardBuilder newInstance(final CMClass entryType) {
		return new CardBuilder(entryType);
	}

	private final Long id;
	private final CMClass type;
	private final String className;
	private final String classDescription;
	private final Long classId;
	private final DateTime begin;
	private final DateTime end;
	private final String user;
	private final Map<String, Object> attributes;

	private final transient String toString;

	public Card(final CardBuilder builder) {
		this.id = builder.id;
		this.type = builder.type;
		this.className = builder.className;
		this.classDescription = builder.classDescription;
		this.classId = builder.classId;
		this.begin = builder.begin;
		this.end = builder.end;
		this.user = builder.user;
		this.attributes = builder.attributes;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getIdentifier() {
		return getId().toString();
	}

	@Override
	public String toString() {
		return toString;
	}

	public Long getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	public String getClassDescription() {
		return classDescription;
	}

	public Long getClassId() {
		return classId;
	}

	public DateTime getBeginDate() {
		return begin;
	}

	public DateTime getEndDate() {
		return end;
	}

	public String getUser() {
		return user;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(final String key) {
		return attributes.get(key);
	}

	public CMClass getType() {
		return type;
	}

	public <T> T getAttribute(final String key, final Class<T> requiredType) {
		return requiredType.cast(attributes.get(key));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classId == null) ? 0 : classId.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Card other = (Card) obj;
		if (classId == null) {
			if (other.classId != null) {
				return false;
			}
		} else if (!classId.equals(other.classId)) {
			return false;
		}
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
