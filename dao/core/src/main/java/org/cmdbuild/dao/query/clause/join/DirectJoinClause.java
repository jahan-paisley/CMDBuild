package org.cmdbuild.dao.query.clause.join;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;

public class DirectJoinClause {

	public static class Builder implements org.cmdbuild.common.Builder<DirectJoinClause> {

		private boolean left = false;
		private CMClass targetClass;
		private Alias targetClassAlias;
		private QueryAliasAttribute sourceAttribute;
		private QueryAliasAttribute targetAttribute;

		@Override
		public DirectJoinClause build() {
			Validate.notNull(targetClass);
			Validate.notNull(sourceAttribute);
			Validate.notNull(targetAttribute);
			if (targetClassAlias == null) {
				targetClassAlias = EntryTypeAlias.canonicalAlias(targetClass);
			}
			return new DirectJoinClause(this);
		}

		public Builder join(final CMClass targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public Builder leftJoin(final CMClass targetClass) {
			this.left = true;
			this.targetClass = targetClass;
			return this;
		}

		public Builder as(final Alias targetClassAlias) {
			this.targetClassAlias = targetClassAlias;
			return this;
		}

		public Builder equalsTo(final QueryAliasAttribute sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
			return this;
		}

		public Builder on(final QueryAliasAttribute targetAttribute) {
			this.targetAttribute = targetAttribute;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final boolean left;
	private final CMClass targetClass;
	private final Alias targetClassAlias;
	private final QueryAliasAttribute sourceAttribute;
	private final QueryAliasAttribute targetAttribute;

	private final transient int hashCode;
	private final transient String toString;

	private DirectJoinClause(final Builder builder) {
		this.left = builder.left;
		this.targetClass = builder.targetClass;
		this.targetClassAlias = builder.targetClassAlias;
		this.sourceAttribute = builder.sourceAttribute;
		this.targetAttribute = builder.targetAttribute;

		this.hashCode = new HashCodeBuilder() //
				.append(left) //
				.append(targetClass) //
				.append(targetClassAlias) //
				.append(sourceAttribute) //
				.append(targetAttribute) //
				.toHashCode();
		this.toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append(left) //
				.append(targetClass) //
				.append(targetClassAlias) //
				.append(sourceAttribute) //
				.append(targetAttribute) //
				.toString();
	}

	public CMClass getTargetClass() {
		return targetClass;
	}

	public Alias getTargetClassAlias() {
		return targetClassAlias;
	}

	public boolean isLeft() {
		return left;
	}

	public QueryAliasAttribute getSourceAttribute() {
		return sourceAttribute;
	}

	public QueryAliasAttribute getTargetAttribute() {
		return targetAttribute;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DirectJoinClause)) {
			return false;
		}
		final DirectJoinClause other = DirectJoinClause.class.cast(obj);
		// TODO
		return new EqualsBuilder() //
				.append(left, other.left) //
				.append(targetClass, other.targetClass) //
				.append(targetClassAlias, other.targetClassAlias) //
				.append(sourceAttribute, other.sourceAttribute) //
				.append(targetAttribute, other.targetAttribute) //
				.isEquals();
	}

	@Override
	public String toString() {
		return toString;
	}

}
